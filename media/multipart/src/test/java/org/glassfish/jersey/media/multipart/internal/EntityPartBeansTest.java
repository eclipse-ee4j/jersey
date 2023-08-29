/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.multipart.internal;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityPartBeansTest extends JerseyTest {

    @Path("/form-field-injected")
    public static class MultiPartFieldInjectedResource {
        @FormParam("string")
        EntityPart stringEntityPart;

        @POST
        @Path("xml-jaxb-part")
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public String post() throws IOException {
            return stringEntityPart.getContent(String.class) + ":" + stringEntityPart.getFileName().get();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MultiPartFieldInjectedResource.class);
    }

    @Test
    public void testInjectedEntityPartBeans() throws IOException {
        final WebTarget target = target().path("/form-field-injected/xml-jaxb-part");

        final EntityPart entityPart = EntityPart.withName("string").fileName("string")
                .content("STRING", String.class)
                .build();

        final GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(List.of(entityPart)) {};

        final String s = target.request().post(Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE), String.class);
        assertEquals("STRING:string", s);
    }
}
