/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.binder.client;

import org.glassfish.jersey.client.ClientBootstrapBag;
import org.glassfish.jersey.client.ClientBootstrapPreinitialization;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.inject.ParameterUpdaterProvider;
import org.glassfish.jersey.client.internal.inject.ParameterUpdaterConfigurator;
import org.glassfish.jersey.inject.weld.ClientTestParent;
import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.ContextResolverFactory;
import org.glassfish.jersey.internal.ExceptionMapperFactory;
import org.glassfish.jersey.internal.JaxrsProviders;
import org.glassfish.jersey.internal.inject.ServiceHolder;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.MessageBodyFactory;
import org.glassfish.jersey.spi.ContextResolvers;
import org.glassfish.jersey.spi.ExceptionMappers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Providers;
import java.util.List;

public class ClientBindingsTest extends ClientTestParent {
    @Test
    void testConfigurationInjection() {
        injectionManager.completeRegistration();
        new ClientBootstrapPreinitialization().preregister(RuntimeType.CLIENT, injectionManager);
        assertOneInstance(Configuration.class, "ClientConfig$State");
    }

    @Test
    void testParamConverterProvider() {
        injectionManager.completeRegistration();
        // new ParamConverterConfigurator().init(injectionManager, (BootstrapBag) null); requires Configuration
        new ClientBootstrapPreinitialization().preregister(RuntimeType.CLIENT, injectionManager);
        assertOneInstance(ParamConverterProvider.class, "AggregatedProvider");
    }

    @Test
    void testParameterUpdaterProvider() {
        injectionManager.completeRegistration();
        new ParameterUpdaterConfigurator().init(injectionManager, new ClientBootstrapBag());
        assertOneInstance(ParameterUpdaterProvider.class, "ParameterUpdaterFactory");
    }

    @Test
    void testContextResolvers() {
        injectionManager.completeRegistration();

        ClientBootstrapBag cbb = new ClientBootstrapBag();
        cbb.setConfiguration(new ClientConfig());
        new ContextResolverFactory.ContextResolversConfigurator().init(injectionManager, cbb);
        assertOneInstance(ContextResolvers.class, "ContextResolverFactory");
    }

    @Test
    void testExceptionMappers() {
        injectionManager.completeRegistration();

        ClientBootstrapBag cbb = new ClientBootstrapBag();
        cbb.setConfiguration(new ClientConfig());
        new ExceptionMapperFactory.ExceptionMappersConfigurator().init(injectionManager, cbb);
        assertOneInstance(ExceptionMappers.class, "ExceptionMapperFactory");
    }

    @Test
    void testProviders() {
        injectionManager.completeRegistration();
        new JaxrsProviders.ProvidersConfigurator().init(injectionManager, (BootstrapBag) null);
        assertOneInstance(Providers.class, "JaxrsProviders");
    }

    @Test
    void testAutoDiscoverable() {
        injectionManager.completeRegistration();
        // new AutoDiscoverableConfigurator(RuntimeType.CLIENT).init(injectionManager, new ClientBootstrapBag()); config
        new ClientBootstrapPreinitialization().preregister(RuntimeType.CLIENT, injectionManager);
        assertMultiple(AutoDiscoverable.class, 2, "LoggingFeatureAutoDiscoverable");
    }

    @Test
    void testMessageBodyWorkers() {
        injectionManager.completeRegistration();
        new ClientBootstrapPreinitialization().preregister(RuntimeType.CLIENT, injectionManager);
        assertOneInstance(MessageBodyWorkers.class, "MessageBodyFactory");
    }

    @Test
    void clientOnlyInstancesTest() {
        injectionManager.completeRegistration();
        List<ClientOnlyPreinitialization.ClientOnlyInterface> instances =
                injectionManager.getAllInstances(ClientOnlyPreinitialization.ClientOnlyInterface.class);
        Assertions.assertEquals(2, instances.size());
        Assertions.assertTrue(instances.stream()
                .anyMatch(p -> p.getClass().equals(ClientOnlyPreinitialization.ClientOnlyClassA.class)));
        Assertions.assertTrue(instances.stream()
                .anyMatch(p -> p.getClass().equals(ClientOnlyPreinitialization.ClientOnlyClassB.class)));
    }

    @Test
    void clientOnlyServiceHoldersTest() {
        injectionManager.completeRegistration();
        List<ServiceHolder<ClientOnlyPreinitialization.ClientOnlyInterface>> instances =
                injectionManager.getAllServiceHolders(ClientOnlyPreinitialization.ClientOnlyInterface.class);
        Assertions.assertEquals(2, instances.size());
        Assertions.assertTrue(instances.stream()
                .anyMatch(p -> p.getInstance().getClass().equals(ClientOnlyPreinitialization.ClientOnlyClassA.class)));
        Assertions.assertTrue(instances.stream()
                .anyMatch(p -> p.getInstance().getClass().equals(ClientOnlyPreinitialization.ClientOnlyClassB.class)));
    }
}
