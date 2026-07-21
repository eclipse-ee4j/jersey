/*
 * Copyright (c) 2022, 2026 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson3.internal.model.Jaxb2ServiceTest;
import org.glassfish.jersey.jackson3.internal.model.ServiceTest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class JacksonJaxb2JsonProviderTest extends JerseyTest {

    @Override
    protected final Application configure() {
        return new ResourceConfig(Jaxb2ServiceTest.class)
                .property(CommonProperties.JSON_JACKSON_DISABLED_MODULES, "NoCtorDeserModule");
    }

    // Exclude NoCtorDeserModule because of dependencies
    @Override
    protected void configureClient(ClientConfig config) {
        config.property(CommonProperties.JSON_JACKSON_DISABLED_MODULES, "NoCtorDeserModule");
    }

    @Test
    public final void testJavaOptional() {
        final String response = target("entity/simple").request().get(String.class);
        assertEquals("{\"name\":\"Hello\",\"value\":\"World\"}", response);
    }

    @Test
    public final void testSimpleGet() {
        final Jaxb2ServiceTest.EntityTest response = target("entity/simple").request().get(Jaxb2ServiceTest.EntityTest.class);
        assertEquals("Hello", response.getName());
        assertEquals("World", response.getValue().orElse(""));
    }

    @Test
    public final void testSimplePost() {
        Entity<Jaxb2ServiceTest.EntityTest> request = Entity.json(new Jaxb2ServiceTest.EntityTest("Hello", "World"));
        try (Response response = target("entity/exchange").request(MediaType.APPLICATION_JSON).post(request)) {
            assertEquals(200, response.getStatus());
            Jaxb2ServiceTest.EntityTest responseEntity = response.readEntity(Jaxb2ServiceTest.EntityTest.class);
            assertEquals("Hello", responseEntity.getName());
            assertEquals("Universe", responseEntity.getValue().orElse(""));
        }
    }

}
