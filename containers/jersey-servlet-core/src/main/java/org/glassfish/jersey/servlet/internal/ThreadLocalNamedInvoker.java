/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * A proxy invocation handler that delegates all methods to a thread local instance from JNDI.
 *
 * @author Paul Sandoz
 */
public class ThreadLocalNamedInvoker<T> extends ThreadLocalInvoker<T> {

    private final String name;

    /**
     * Create an instance.
     *
     * @param name the JNDI name at which an instance of T can be found.
     */
    public ThreadLocalNamedInvoker(final String name) {
        this.name = name;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // if no instance yet exists for the current thread then look one up and stash it
        if (this.get() == null) {
            Context ctx = new InitialContext();
            T t = (T) ctx.lookup(name);
            this.set(t);
        }
        return super.invoke(proxy, method, args);
    }
}
