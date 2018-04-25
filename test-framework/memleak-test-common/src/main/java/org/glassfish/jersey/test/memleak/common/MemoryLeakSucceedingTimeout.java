/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.test.memleak.common;

import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class MemoryLeakSucceedingTimeout extends Timeout {

    private static final int DEFAULT_TIMEOUT_MILLIS = 300_000;
    private int millis;

    public MemoryLeakSucceedingTimeout() {
        this(DEFAULT_TIMEOUT_MILLIS);
    }

    public MemoryLeakSucceedingTimeout(final int defaultMillisTimeout) {
        super(defaultMillisTimeout);

        this.millis = Integer.getInteger(MemoryLeakUtils.JERSEY_CONFIG_TEST_MEMLEAK_TIMEOUT, defaultMillisTimeout);
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new FailOnTimeout(base, millis) {
            @Override
            public void evaluate() throws Throwable {
                try {
                    super.evaluate();
                } catch (Throwable throwable) {
                    if (throwable.getMessage().startsWith("test timed out after")) {
                        MemoryLeakUtils.verifyNoOutOfMemoryOccurred();
                        System.out.println("Test timed out after " + millis + " ms. Successfully ending.");
                    } else {
                        throw throwable;
                    }
                }
            }
        };
    }
}
