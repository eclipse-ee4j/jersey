/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.ws.rs.Path;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Constantino Cronemberger (ccronemberger at yahoo.com.br)
 */
public class ModelHelperTest {

    @Test
    public void testClass() {
        final Class cls = ModelHelper.getAnnotatedResourceClass(MyAnnotatedClass.class);
        Assert.assertSame(MyAnnotatedClass.class, cls);
    }

    @Test
    public void testSubClass() {
        // Spring with CGLIB proxies creates sub-classes
        final Object obj = new MyAnnotatedClass() {};
        Assert.assertNotSame(MyAnnotatedClass.class, obj.getClass());
        final Class cls = ModelHelper.getAnnotatedResourceClass(obj.getClass());
        Assert.assertSame(MyAnnotatedClass.class, cls);
    }

    @Test
    public void testProxyClass() throws Exception {
        // Spring can also create proxies for beans
        final Object obj = Proxy
                .newProxyInstance(getClass().getClassLoader(), new Class[] {MyServiceInterface.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        return null;
                    }
                });
        final Class cls = ModelHelper.getAnnotatedResourceClass(obj.getClass());
        Assert.assertSame(MyServiceInterface.class, cls);
    }

    @Path("test")
    public interface MyServiceInterface {}

    @Path("test")
    public static class MyAnnotatedClass implements MyServiceInterface {}
}
