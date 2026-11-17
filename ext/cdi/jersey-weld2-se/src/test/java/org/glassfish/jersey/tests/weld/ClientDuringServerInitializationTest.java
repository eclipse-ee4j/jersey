/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.jersey.tests.weld;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
import org.glassfish.jersey.ext.cdi1x.internal.ProcessAllAnnotatedTypes;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.spi.ContextResolvers;
import org.glassfish.jersey.weld.se.WeldRequestScope;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import org.junit.jupiter.api.Test;

import static jakarta.ws.rs.RuntimeType.SERVER;
import static jakarta.ws.rs.client.ClientBuilder.newClient;
import static jakarta.ws.rs.core.MediaType.WILDCARD_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Regression test for Jersey issue #6155.
 *
 * @see <a href="https://github.com/eclipse-ee4j/jersey/issues/6155">Issue 6155</a>
 */
public class ClientDuringServerInitializationTest {

    @Test
    public void testServerConfigurationInjection() {
        test(ServerApplication.class);
    }

    @Test
    public void testServerConfigurationInjectionAfterClientCloses() {
        test(ClientStartingApplication.class);
    }

    private static void test(Class<? extends Application> applicationClass) {
        Weld weld = new Weld()
            .disableDiscovery()
            .addBeanClasses(applicationClass, ConfigurationResolver.class, WeldRequestScope.class)
            .addExtensions(new CdiComponentProvider(), new ProcessAllAnnotatedTypes());
        try (WeldContainer container = weld.initialize()) {
            ApplicationHandler handler = new ApplicationHandler(applicationClass);
            try {
                ContextResolver<Configuration> resolver = handler.getInjectionManager()
                    .getInstance(ContextResolvers.class)
                    .resolve(Configuration.class, WILDCARD_TYPE);
                assertNotNull(resolver);
                Configuration configuration = resolver.getContext(Configuration.class);
                assertNotNull(configuration);
                assertEquals(SERVER, configuration.getRuntimeType());
            } finally {
                handler.getInjectionManager().shutdown();
            }
        }
    }

    @ApplicationScoped
    public static class ServerApplication extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            return Set.of(ConfigurationResolver.class);
        }
    }

    @ApplicationScoped
    public static class ClientStartingApplication extends ServerApplication {

        @Override
        public Set<Class<?>> getClasses() {
            try (Client client = newClient()) {
                // Initialize the client runtime without sending an HTTP request.
                client.target("http://localhost").request();
            }
            return super.getClasses();
        }
    }

    @Provider
    public static class ConfigurationResolver implements ContextResolver<Configuration> {

        @Context
        private Configuration configuration;

        @Override
        public Configuration getContext(Class<?> type) {
            return configuration;
        }
    }

}
