/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.internal;

import java.util.Collections;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.ContextResolverFactory;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.inject.ProviderBinder;
import org.glassfish.jersey.tests.e2e.common.TestRuntimeDelegate;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Context resolvers factory unit test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ContextResolverFactoryTest {

    @Provider
    private static class CustomStringResolver implements ContextResolver<String> {

        public static final String VALUE = "foof";

        @Override
        public String getContext(Class<?> type) {
            return VALUE;
        }
    }

    @Provider
    @Produces("application/*")
    private static class CustomIntegerResolverA implements ContextResolver<Integer> {

        public static final int VALUE = 1001;

        @Override
        public Integer getContext(Class<?> type) {
            return VALUE;
        }
    }

    @Provider
    @Produces("application/json")
    private static class CustomIntegerResolverB implements ContextResolver<Integer> {

        public static final int VALUE = 2002;

        @Override
        public Integer getContext(Class<?> type) {
            return VALUE;
        }
    }

    @Provider
    @Produces("application/json")
    private static class CustomIntegerResolverC implements ContextResolver<Integer> {

        public static final int VALUE = 3003;

        @Override
        public Integer getContext(Class<?> type) {
            return VALUE;
        }
    }

    private static class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(CustomStringResolver.class).to(ContextResolver.class);
            bind(CustomIntegerResolverA.class).to(ContextResolver.class);
            bind(CustomIntegerResolverB.class).to(ContextResolver.class);
        }
    }

    private ContextResolverFactory crf;

    public ContextResolverFactoryTest() {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    @Before
    public void setUp() {
        InjectionManager injectionManager = Injections.createInjectionManager();
        ProviderBinder providerBinder = new ProviderBinder(injectionManager);
        providerBinder.bindClasses(Collections.singleton(CustomIntegerResolverC.class));
        injectionManager.register(new Binder());

        BootstrapBag bootstrapBag = new BootstrapBag();
        ContextResolverFactory.ContextResolversConfigurator configurator =
                new ContextResolverFactory.ContextResolversConfigurator();
        configurator.init(injectionManager, bootstrapBag);
        injectionManager.completeRegistration();
        configurator.postInit(injectionManager, bootstrapBag);

        crf = injectionManager.getInstance(ContextResolverFactory.class);
    }

    @Test
    public void testResolve() {
        assertEquals(CustomStringResolver.VALUE, crf.resolve(String.class, MediaType.WILDCARD_TYPE).getContext(String.class));
        assertEquals(CustomStringResolver.VALUE, crf.resolve(String.class, MediaType.TEXT_PLAIN_TYPE).getContext(String.class));

        assertEquals(CustomIntegerResolverA.VALUE,
                crf.resolve(Integer.class, MediaType.APPLICATION_XML_TYPE).getContext(Integer.class));
        assertEquals(CustomIntegerResolverA.VALUE,
                crf.resolve(Integer.class, MediaType.valueOf("application/*")).getContext(Integer.class));

        // Test that resolver "B" is shadowed by a custom resolver "C"
        assertEquals(CustomIntegerResolverC.VALUE,
                crf.resolve(Integer.class, MediaType.APPLICATION_JSON_TYPE).getContext(Integer.class));

        // Test that there is no matching provider
        assertNull(crf.resolve(Integer.class, MediaType.TEXT_PLAIN_TYPE));
    }
}
