/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources;

import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.inject.spi.Extension;

/**
 * Part of JERSEY-2461 reproducer. We need an extension that we could inject,
 * to make sure HK2 custom binding does not attempt to mess up.
 *
 * @author Jakub Podlesak
 */
public class CustomExtension implements Extension {

    private AtomicInteger counter = new AtomicInteger();

    /**
     * A made up functionality. Does not really matter. CDI
     * would refuse to deploy the application if something went wrong.
     *
     * @return next count.
     */
    public int getCount() {
        return counter.incrementAndGet();
    }
}
