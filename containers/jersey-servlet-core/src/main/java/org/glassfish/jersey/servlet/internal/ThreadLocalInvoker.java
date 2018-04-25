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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A proxy invocation handler that delegates all methods to a thread local instance.
 *
 * @author Paul Sandoz
 */
public class ThreadLocalInvoker<T> implements InvocationHandler {

    private ThreadLocal<T> threadLocalInstance = new ThreadLocal<>();

    public void set(final T threadLocalInstance) {
        this.threadLocalInstance.set(threadLocalInstance);
    }

    public T get() {
        return this.threadLocalInstance.get();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (threadLocalInstance.get() == null) {
            throw new IllegalStateException(LocalizationMessages.PERSISTENCE_UNIT_NOT_CONFIGURED(proxy.getClass()));
        }

        try {
            return method.invoke(threadLocalInstance.get(), args);
        } catch (final IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (final InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
}
