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

package org.glassfish.jersey.media.multipart.internal;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.MultiPartProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class RestrictionsTest extends JerseyTest {
    @Path("/")
    public static class RestrictionsTestResource {
        @POST
        @Path("max.parts")
        public String postMaxPart(@FormDataParam("part1") String part1, @FormDataParam("part2") String part2) {
            return part1 + part2;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(RestrictionsTestResource.class)
                .register(MultiPartFeature.class)
                .register(new MultiPartProperties().maxParts(2).resolver());
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class);
    }

    @Test
    public void testPassNumberOfParts() {
        FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.field("part1", "he", MediaType.TEXT_PLAIN_TYPE);
        multiPart.field("part2", "llo", MediaType.TEXT_PLAIN_TYPE);
        try (Response r = target("max.parts").request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE))) {
            Assertions.assertEquals(200, r.getStatus());
            Assertions.assertEquals("hello", r.readEntity(String.class));
        }
    }

    @Test
    public void testFailsNumberOfParts() {
        FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.field("part1", "he", MediaType.TEXT_PLAIN_TYPE);
        multiPart.field("part2", "llo", MediaType.TEXT_PLAIN_TYPE);
        multiPart.field("part3", "!", MediaType.TEXT_PLAIN_TYPE);
        try (Response r = target("max.parts").request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE))) {
            Assertions.assertEquals(Response.Status.REQUEST_ENTITY_TOO_LARGE.getStatusCode(), r.getStatus());
        }
    }
}
