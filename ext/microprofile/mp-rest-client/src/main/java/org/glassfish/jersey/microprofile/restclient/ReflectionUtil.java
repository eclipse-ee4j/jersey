/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author David Kral
 * @author Tomas Langer
 */
final class ReflectionUtil {

    private ReflectionUtil() {
    }

    static <T> T createInstance(Class<T> tClass) {
        return AccessController.doPrivileged((PrivilegedAction<T>) () -> {
            try {
                return tClass.getConstructor().newInstance();
            } catch (Throwable t) {
                throw new RuntimeException("No default constructor in class " + tClass + " present. Class cannot be created!", t);
            }
        });
    }

    @SuppressWarnings("unchecked")
    static <T> T createProxyInstance(Class<T> restClientClass) {
        return AccessController.doPrivileged((PrivilegedAction<T>) () -> (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {restClientClass},
                (proxy, m, args) -> {
                    Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                            .getDeclaredConstructor(Class.class);
                    constructor.setAccessible(true);
                    return constructor.newInstance(restClientClass)
                            .in(restClientClass)
                            .unreflectSpecial(m, restClientClass)
                            .bindTo(proxy)
                            .invokeWithArguments(args);
                }));
    }

}
