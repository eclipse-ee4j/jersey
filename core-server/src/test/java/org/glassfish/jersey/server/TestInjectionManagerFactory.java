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

package org.glassfish.jersey.server;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.internal.AutoDiscoverableConfigurator;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.ContextResolverFactory;
import org.glassfish.jersey.internal.ExceptionMapperFactory;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.message.internal.MessageBodyFactory;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.model.internal.ManagedObjectsFinalizer;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.server.internal.inject.ParamConverterConfigurator;
import org.glassfish.jersey.server.internal.inject.ParamExtractorConfigurator;
import org.glassfish.jersey.server.internal.inject.ValueParamProviderConfigurator;
import org.glassfish.jersey.server.internal.process.RequestProcessingConfigurator;
import org.glassfish.jersey.server.model.internal.ResourceMethodInvokerConfigurator;

/**
 * Utility class to create initialized server-side injection manager.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class TestInjectionManagerFactory {

    private static Binder EMPTY_BINDER = new AbstractBinder() {
        @Override
        protected void configure() {
        }
    };

    private TestInjectionManagerFactory() {
        // prevents instantiation
    }

    /**
     * Create new initialized server injection manager.
     *
     * @return new initialized server injection manager.
     */
    public static BootstrapResult createInjectionManager() {
        return createInjectionManager(new ResourceConfig(), EMPTY_BINDER);
    }

    /**
     * Create new initialized server injection manager.
     *
     * @param runtimeConfig runtime config with test's components.
     * @return new initialized server injection manager.
     */
    public static BootstrapResult createInjectionManager(ResourceConfig runtimeConfig) {
        return createInjectionManager(runtimeConfig, EMPTY_BINDER);
    }

    /**
     * Create new initialized server injection manager.
     *
     * @param runtimeConfig runtime config with test's components.
     * @param customBinder  binder which is immediately registered.
     * @return new initialized server injection manager.
     */
    public static BootstrapResult createInjectionManager(ResourceConfig runtimeConfig,  Binder customBinder) {
        InjectionManager injectionManager = Injections.createInjectionManager();
        injectionManager.register(new ServerBinder());
        injectionManager.register(new MessagingBinders.MessageBodyProviders(runtimeConfig.getProperties(), RuntimeType.SERVER));
        injectionManager.register(customBinder);

        ServerBootstrapBag bootstrapBag = new ServerBootstrapBag();
        ManagedObjectsFinalizer managedObjectsFinalizer = new ManagedObjectsFinalizer(injectionManager);
        bootstrapBag.setManagedObjectsFinalizer(managedObjectsFinalizer);

        List<BootstrapConfigurator> bootstrapConfigurators = Arrays.asList(
                new RequestProcessingConfigurator(),
                new RequestScope.RequestScopeConfigurator(),
                new ParamConverterConfigurator(),
                new ParamExtractorConfigurator(),
                new ValueParamProviderConfigurator(),
                new JerseyResourceContextConfigurator(),
                new ComponentProviderConfigurator(),
                new TestConfigConfigurator(runtimeConfig),
                new ContextResolverFactory.ContextResolversConfigurator(),
                new MessageBodyFactory.MessageBodyWorkersConfigurator(),
                new ExceptionMapperFactory.ExceptionMappersConfigurator(),
                new ResourceMethodInvokerConfigurator(),
                new ProcessingProvidersConfigurator(),
                new ContainerProviderConfigurator(RuntimeType.SERVER),
                new AutoDiscoverableConfigurator(RuntimeType.SERVER),
                new ResourceBagConfigurator(),
                new ExternalRequestScopeConfigurator(),
                new ModelProcessorConfigurator(),
                new ResourceModelConfigurator(),
                new ServerExecutorProvidersConfigurator());

        bootstrapConfigurators.forEach(configurator -> configurator.init(injectionManager, bootstrapBag));

        // Configure binders and features.
        bootstrapBag.getRuntimeConfig().configureMetaProviders(injectionManager, bootstrapBag.getManagedObjectsFinalizer());

        injectionManager.completeRegistration();
        bootstrapConfigurators.forEach(configurator -> configurator.postInit(injectionManager, bootstrapBag));
        return new BootstrapResult(injectionManager, bootstrapBag);
    }

    public static class BootstrapResult {
        public InjectionManager injectionManager;
        public ServerBootstrapBag bootstrapBag;

        BootstrapResult(InjectionManager injectionManager, ServerBootstrapBag bootstrapBag) {
            this.injectionManager = injectionManager;
            this.bootstrapBag = bootstrapBag;
        }
    }
}
