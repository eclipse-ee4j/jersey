/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet.internal;

import java.lang.reflect.Proxy;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michal Gajdos
 */
public class ThreadLocalInvokerTest {

    public static class CheckedException extends Exception {

    }

    public static interface X {

        public String checked() throws CheckedException;

        public String runtime();
    }

    @Test
    public void testIllegalState() {
        final ThreadLocalInvoker<X> tli = new ThreadLocalInvoker<>();

        final X x = (X) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{X.class}, tli);

        boolean caught = false;
        try {
            x.checked();
        } catch (final Exception ex) {
            caught = true;
            assertEquals(IllegalStateException.class, ex.getClass());
        }
        assertTrue(caught);

        caught = false;
        try {
            x.runtime();
        } catch (final Exception ex) {
            caught = true;
            assertEquals(IllegalStateException.class, ex.getClass());
        }
        assertTrue(caught);
    }

    @Test
    public void testExceptions() {
        final ThreadLocalInvoker<X> tli = new ThreadLocalInvoker<>();

        final X x = (X) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{X.class}, tli);

        tli.set(new X() {
            public String checked() throws CheckedException {
                throw new CheckedException();
            }

            public String runtime() {
                throw new RuntimeException();
            }
        });

        boolean caught = false;
        try {
            x.checked();
        } catch (final Exception ex) {
            caught = true;
            assertEquals(CheckedException.class, ex.getClass());
        }
        assertTrue(caught);

        caught = false;
        try {
            x.runtime();
        } catch (final Exception ex) {
            caught = true;
            assertEquals(RuntimeException.class, ex.getClass());
        }
        assertTrue(caught);
    }
}
