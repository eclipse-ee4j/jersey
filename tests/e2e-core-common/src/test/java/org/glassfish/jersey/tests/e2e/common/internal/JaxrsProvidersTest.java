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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.ContextResolverFactory;
import org.glassfish.jersey.internal.ExceptionMapperFactory;
import org.glassfish.jersey.internal.JaxrsProviders;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.message.internal.MessageBodyFactory;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.tests.e2e.common.TestRuntimeDelegate;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**®
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class JaxrsProvidersTest {

    private static class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(new ContextResolver<String>() {
                @Override
                public String getContext(Class<?> type) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }).to(new GenericType<ContextResolver<String>>() {
            });

            bind(new CommonConfig(RuntimeType.SERVER, ComponentBag.EXCLUDE_EMPTY)).to(Configuration.class);
        }
    }

    public JaxrsProvidersTest() {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    @Test
    public void testProviders() throws Exception {
        InjectionManager injectionManager = Injections.createInjectionManager();
        injectionManager.register(new MessagingBinders.MessageBodyProviders(null, RuntimeType.SERVER));
        injectionManager.register(new Binder());

        BootstrapBag bootstrapBag = new BootstrapBag();
        List<BootstrapConfigurator> bootstrapConfigurators = Arrays.asList(
                new RequestScope.RequestScopeConfigurator(),
                new TestConfigConfigurator(),
                new ContextResolverFactory.ContextResolversConfigurator(),
                new MessageBodyFactory.MessageBodyWorkersConfigurator(),
                new ExceptionMapperFactory.ExceptionMappersConfigurator(),
                new JaxrsProviders.ProvidersConfigurator());
        injectionManager.register(new TestBinder());

        TestBinder.initProviders(injectionManager);
        bootstrapConfigurators.forEach(configurator -> configurator.init(injectionManager, bootstrapBag));
        injectionManager.completeRegistration();
        bootstrapConfigurators.forEach(configurator -> configurator.postInit(injectionManager, bootstrapBag));

        RequestScope scope = bootstrapBag.getRequestScope();

        scope.runInScope((Callable<Object>) () -> {
            Providers instance = injectionManager.getInstance(Providers.class);

            assertNotNull(instance);
            assertSame(JaxrsProviders.class, instance.getClass());

            assertNotNull(instance.getExceptionMapper(Throwable.class));
            assertNotNull(instance.getMessageBodyReader(String.class, String.class, new Annotation[0],
                    MediaType.TEXT_PLAIN_TYPE));
            assertNotNull(instance.getMessageBodyWriter(String.class, String.class, new Annotation[0],
                    MediaType.TEXT_PLAIN_TYPE));
            assertNotNull(instance.getContextResolver(String.class, MediaType.TEXT_PLAIN_TYPE));
            return null;
        });
    }
}
