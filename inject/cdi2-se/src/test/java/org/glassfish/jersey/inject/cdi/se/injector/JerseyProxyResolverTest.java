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

package org.glassfish.jersey.inject.cdi.se.injector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.JaxrsProviders;
import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjecteeImpl;
import org.glassfish.jersey.internal.inject.InjectionResolver;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests Jersey Proxy Resolver.
 */
public class JerseyProxyResolverTest {

    private static Field[] FIELDS = StubForFields.class.getDeclaredFields();

    @Test
    public void testSignletonIsProxiable() {
        InjecteeImpl injectee = new InjecteeImpl();
        injectee.setInjecteeClass(TestSingleton.class);
        injectee.setParentClassScope(Singleton.class);

        JerseyProxyResolver resolver = new JerseyProxyResolver();
        assertTrue(resolver.isPrixiable(injectee));
    }

    @Test
    public void testApplicationScopeIsProxiable() {
        InjecteeImpl injectee = new InjecteeImpl();
        injectee.setInjecteeClass(TestApplicationScope.class);
        injectee.setParentClassScope(ApplicationScoped.class);

        JerseyProxyResolver resolver = new JerseyProxyResolver();
        assertTrue(resolver.isPrixiable(injectee));
    }

    @Test
    public void testRequestScopeFromNonAnnotatedIsNotProxiable() {
        InjecteeImpl injectee = new InjecteeImpl();
        injectee.setInjecteeClass(TestNonAnnotatedRequestScope.class);
        injectee.setParentClassScope(RequestScoped.class);

        JerseyProxyResolver resolver = new JerseyProxyResolver();
        assertFalse(resolver.isPrixiable(injectee));
    }

    @Test
    public void testRequestScopeIsNotProxiable() {
        InjecteeImpl injectee = new InjecteeImpl();
        injectee.setInjecteeClass(TestRequestScope.class);
        injectee.setParentClassScope(RequestScoped.class);

        JerseyProxyResolver resolver = new JerseyProxyResolver();
        assertFalse(resolver.isPrixiable(injectee));
    }

    @Test
    public void testApplicationIsNotProxiable() {
        InjecteeImpl injectee = new InjecteeImpl();
        injectee.setRequiredType(Application.class);

        JerseyProxyResolver resolver = new JerseyProxyResolver();
        assertFalse(resolver.isPrixiable(injectee));
    }

    @Test
    public void testProxyCreated() {
        MyInjectionResolver injectionResolver = new MyInjectionResolver(new JaxrsProviders());
        InjecteeImpl injectee = new InjecteeImpl();
        injectee.setRequiredType(Providers.class);
        injectee.setParent(FIELDS[0]);

        JerseyProxyResolver resolver = new JerseyProxyResolver();
        Object proxy = resolver.proxy(injectee, injectionResolver);
        assertTrue(proxy.getClass().getName().contains("Proxy"));
    }

    @Test
    public void testProxyCached() {
        MyInjectionResolver injectionResolver = new MyInjectionResolver(new JaxrsProviders());
        InjecteeImpl injectee1 = new InjecteeImpl();
        injectee1.setRequiredType(Providers.class);
        injectee1.setParent(FIELDS[0]);

        InjecteeImpl injectee2 = new InjecteeImpl();
        injectee2.setRequiredType(Providers.class);
        injectee2.setParent(FIELDS[1]);

        JerseyProxyResolver resolver = new JerseyProxyResolver();
        Object proxy1 = resolver.proxy(injectee1, injectionResolver);
        Object proxy2 = resolver.proxy(injectee2, injectionResolver);
        assertSame(proxy1.getClass(), proxy2.getClass());
    }

    @Test
    public void testProxyCacheNotMismatched() {
        MyInjectionResolver injectionResolver1 = new MyInjectionResolver(new JaxrsProviders());
        InjecteeImpl injectee1 = new InjecteeImpl();
        injectee1.setRequiredType(Providers.class);
        injectee1.setParent(FIELDS[0]);

        MyInjectionResolver injectionResolver2 = new MyInjectionResolver(new ArrayList<>());
        InjecteeImpl injectee2 = new InjecteeImpl();
        injectee2.setRequiredType(List.class);
        injectee2.setParent(FIELDS[1]);

        JerseyProxyResolver resolver = new JerseyProxyResolver();
        Object proxy1 = resolver.proxy(injectee1, injectionResolver1);
        Object proxy2 = resolver.proxy(injectee2, injectionResolver2);
        assertNotSame(proxy1.getClass(), proxy2.getClass());
    }

    private static class StubForFields {
        private Object field1;
        private Object field2;
    }

    private static class MyInjectionResolver implements InjectionResolver {

        private final Object instance;

        private MyInjectionResolver(Object instance) {
            this.instance = instance;
        }

        @Override
        public Object resolve(Injectee injectee) {
            return instance;
        }

        @Override
        public boolean isConstructorParameterIndicator() {
            return true;
        }

        @Override
        public boolean isMethodParameterIndicator() {
            return false;
        }

        @Override
        public Class getAnnotation() {
            return Context.class;
        }
    }

    private static class TestNonAnnotatedRequestScope {
    }

    @RequestScoped
    private static class TestRequestScope {
    }

    @Singleton
    private static class TestSingleton {
    }

    @ApplicationScoped
    private static class TestApplicationScope {
    }
}
