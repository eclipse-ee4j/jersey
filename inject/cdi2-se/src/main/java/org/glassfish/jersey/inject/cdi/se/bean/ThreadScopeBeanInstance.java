/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.cdi.se.bean;

import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.bean.proxy.ContextBeanInstance;

import java.lang.reflect.Method;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.PassivationCapable;

/**
 * {@link org.glassfish.jersey.internal.inject.PerThread} scope bean instance used from
 * {@link SupplierThreadScopeBean} and {@link SupplierThreadScopeClassBean}.
 *
 * @param <T> Typed of the bean supplied by a {@code Supplier}.
 */
class ThreadScopeBeanInstance<T> extends ContextBeanInstance<T> {

    private final WeakHashMap<Thread, Object> instances = new WeakHashMap<>();

    private final Supplier<T> supplier;

    /**
     * Creates a new invocation handler with supplier which provides a current injected value in proper scope.
     *
     * @param supplier provider of the value.
     */
    ThreadScopeBeanInstance(Supplier<T> supplier, Bean<T> bean, String contextId) {
        super(bean, new StringBeanIdentifier(((PassivationCapable) bean).getId()), contextId);
        this.supplier = supplier;
    }

    @Override
    public Object invoke(Object obj, Method method, Object... arguments) throws Throwable {
        Object instance = instances.computeIfAbsent(Thread.currentThread(), thread -> supplier.get());
        return super.invoke(instance, method, arguments);
    }

    public void dispose() {
        this.instances.clear();
    }
}
