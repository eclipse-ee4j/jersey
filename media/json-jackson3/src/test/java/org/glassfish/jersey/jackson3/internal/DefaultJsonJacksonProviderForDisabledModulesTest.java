/*
 * Copyright (c) 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson3.internal;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Providers;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson3.JacksonFeature;
import org.glassfish.jersey.jackson3.internal.model.NoCtorPojo;
import org.glassfish.jersey.jackson3.internal.model.NoCtorServiceTest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DatabindException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test to verify that disabled modules are registered. Should not able to deserialize NoCtorResponse
public class DefaultJsonJacksonProviderForDisabledModulesTest extends JerseyTest {

    @Override
    protected final Application configure() {
        return new ResourceConfig(NoCtorServiceTest.class)
                .property(CommonProperties.JSON_JACKSON_DISABLED_MODULES, "NoCtorDeserModule");
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class)
                .register(TestJacksonXmlBindJsonProvider.class)
                .property(CommonProperties.JSON_JACKSON_DISABLED_MODULES, "NoCtorDeserModule");
    }

    @Test
    public final void testDisabledModule() {
        try (Response response = target("entity/simple").request().get()) {
            assertEquals(200, response.getStatus());
            assertThrows(DatabindException.class, () -> response.readEntity(NoCtorPojo.class));
        }
    }

    @Test
    public final void testExchangeDisabledModule() {
        Entity<NoCtorPojo> request = Entity.json(new NoCtorPojo("Hello", "World"));
        try (Response response = target("entity/exchange").request(MediaType.APPLICATION_JSON).post(request)) {
            assertEquals(400, response.getStatus());
            assertTrue(response.readEntity(String.class).startsWith("Cannot construct instance of"));
        }
    }


    private static class TestJacksonXmlBindJsonProvider extends DefaultJacksonXmlBindJsonProvider {

        private final Configuration configuration;

        @Inject
        public TestJacksonXmlBindJsonProvider(@Context Providers providers, @Context Configuration configuration) {
            super(providers, configuration);
            this.configuration = configuration;
        }

        @PostConstruct
        public void checkModulesCount() {
            final String disabledModules =
                    CommonProperties.getValue(configuration.getProperties(),
                            configuration.getRuntimeType(),
                            CommonProperties.JSON_JACKSON_DISABLED_MODULES, String.class);
            assertEquals("NoCtorDeserModule", disabledModules);
        }

    }


}