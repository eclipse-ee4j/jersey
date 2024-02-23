/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson.internal;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.model.JAXBServiceTest;
import org.glassfish.jersey.jackson.internal.model.ServiceTest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DefaultJsonJacksonProviderForDisabledModulesTest extends JerseyTest {
    @Override
    protected final Application configure() {
        return new ResourceConfig(ServiceTest.class, JAXBServiceTest.class)
                .property("jersey.config.json.jackson.disabled.modules", "Jdk8Module");
    }

    @Test
    public final void testDisabledModule() {
        getClient()
                .register(JacksonFeature.class)
                .register(TestJacksonJaxbJsonProvider.class)
                .property("jersey.config.json.jackson.disabled.modules", "Jdk8Module");
        try (Response response = target("JAXBEntity").request().get()) {
            assertEquals(400, response.getStatus());
            assertNotEquals("{\"key\":\"key\",\"value\":\"value\"}", response.readEntity(String.class));
        }
    }

    private static class TestJacksonJaxbJsonProvider extends JacksonJaxbJsonProvider {

        @Inject
        public TestJacksonJaxbJsonProvider(@Context Configuration configuration) {
            this.configuration = configuration;
        }
        private Configuration configuration;

        @PostConstruct
        public void checkModulesCount() {
            final String disabledModules =
                    CommonProperties.getValue(configuration.getProperties(),
                            configuration.getRuntimeType(),
                            CommonProperties.JSON_JACKSON_DISABLED_MODULES, String.class);
            assertEquals("Jdk8Module", disabledModules);
        }

    }

}