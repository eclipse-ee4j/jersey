/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.cdi.se;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.inject.Vetoed;

import org.glassfish.jersey.internal.inject.DisposableSupplier;

@Vetoed
public class DisposableSupplierImpl implements DisposableSupplier<String> {
    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public String get() {
        // Create a new string - don't share the instances in the string pool.
        return new String(counter.incrementAndGet() + "");
    }

    @Override
    public void dispose(final String instance) {
        counter.decrementAndGet();
    }
}
