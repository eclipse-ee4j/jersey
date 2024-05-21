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

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.ParamQualifier;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderParamTest extends JerseyTest {
    @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @ParamQualifier
    public static @interface AnnoWithValue {
        String value() default "";
    }

    @Path("/order")
    public static class OrderTestResource {
        @POST
        @Path("/dataAfter")
        @Consumes(value = MediaType.MULTIPART_FORM_DATA)
        public String orderBefore(@FormDataParam("file") @AnnoWithValue("xxx") InputStream inputStream) throws IOException {
            return ReaderWriter.readFromAsString(inputStream, MediaType.TEXT_PLAIN_TYPE);
        }

        @POST
        @Path("/dataBefore")
        @Consumes(value = MediaType.MULTIPART_FORM_DATA)
        public String orderAfter(@AnnoWithValue("zzz") @FormDataParam("file") InputStream inputStream) throws IOException {
            return ReaderWriter.readFromAsString(inputStream, MediaType.TEXT_PLAIN_TYPE);
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(OrderTestResource.class).register(MultiPartFeature.class);
    }

    @Test
    public void testOrder() {
        final String MSG = "Hello";
        FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.field("file", MSG, MediaType.TEXT_PLAIN_TYPE);
        try (Response response = target("order")
                .register(MultiPartFeature.class)
                .path("dataBefore").request()
                .post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE))) {
            assertEquals(200, response.getStatus());
            assertEquals(MSG, response.readEntity(String.class));
        }

        try (Response response = target("order")
                .register(MultiPartFeature.class)
                .path("dataAfter").request()
                .post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE))) {
            assertEquals(200, response.getStatus());
            assertEquals(MSG, response.readEntity(String.class));
        }
    }
}
