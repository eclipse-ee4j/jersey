/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.MessageProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

public class StreamReadConstrainsTest extends JerseyTest {
    private static final String ERROR_MSG_PART = "maximum allowed (";

    @Override
    protected final Application configure() {
        return new ResourceConfig(TestLengthResource.class,
                MyStreamReadConstraints.class,
                MyStreamReadConstraintsExceptionMapper.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class);
    }

    @Test
    void testNumberLength() {
        try (Response response = target("len/entity").request()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(new MyEntity(3), MediaType.APPLICATION_JSON_TYPE))) {
            Assertions.assertEquals(200, response.getStatus());
            JsonNode entity = response.readEntity(JsonNode.class);
            Assertions.assertEquals("1234", entity.get("value").asText());
        }

        try (Response response = target("len/entity").request()
                .post(Entity.entity(new MyEntity(8), MediaType.APPLICATION_JSON_TYPE))) {
            Assertions.assertEquals(200, response.getStatus());
            String errorMsg = response.readEntity(String.class);
            Assertions.assertTrue(errorMsg.contains(ERROR_MSG_PART + 4));
        }
    }

    @Test
    void testStringLengthUsingProperty() {
        testConstraintOnClient(
                client()
                        .property(MessageProperties.JSON_MAX_STRING_LENGTH, 4)
                        .target(getBaseUri())
                        .path("len/strlen"),
                4
        );
    }

    @Test
    void testStringLengthPriorityProperty() {
        testConstraintOnClient(
                ClientBuilder.newClient()
                    .register(JacksonFeature.withExceptionMappers().maxStringLength(30))
                    .property(MessageProperties.JSON_MAX_STRING_LENGTH, "3" /* check string value */)
                    .target(getBaseUri()).path("len/strlen"),
                3);
    }

    @Test
    void testStringLengthUsingFeature() {
        testConstraintOnClient(
                ClientBuilder.newClient()
                        .register(JacksonFeature.withExceptionMappers().maxStringLength(3))
                        .target(getBaseUri())
                        .path("len/strlen"),
                3
        );
    }

    void testConstraintOnClient(WebTarget target, int expectedLength) {
        try (Response response = target.request().post(Entity.entity(expectedLength + 1, MediaType.APPLICATION_JSON_TYPE))) {
            Assertions.assertEquals(200, response.getStatus());

            JsonNode errorMsg = response.readEntity(JsonNode.class);
            Assertions.fail("StreamConstraintsException has not been thrown");
        } catch (ProcessingException ex) {
            if (!StreamConstraintsException.class.isInstance(ex.getCause())) {
                throw ex;
            }
            String errorMsg = ex.getCause().getMessage();
            Assertions.assertTrue(errorMsg.contains(ERROR_MSG_PART + String.valueOf(expectedLength)));
        }
    }

    @Test
    void testMatchingVersion() {
        final Version coreVersion = PackageVersion.VERSION;
        final Version jerseyVersion = org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.PackageVersion.VERSION;

        StringBuilder message = new StringBuilder();
        message.append("Dependency Jackson Version is ")
                .append(coreVersion.getMajorVersion())
                .append(".")
                .append(coreVersion.getMinorVersion());
        message.append("\n Repackaged Jackson Version is ")
                .append(jerseyVersion.getMajorVersion())
                .append(".")
                .append(jerseyVersion.getMinorVersion());

        Assertions.assertEquals(coreVersion.getMajorVersion(), jerseyVersion.getMajorVersion(), message.toString());
        Assertions.assertEquals(coreVersion.getMinorVersion(), jerseyVersion.getMinorVersion(), message.toString());
        Assertions.assertEquals(coreVersion.getMajorVersion(), 2,
                "update " + DefaultJacksonJaxbJsonProvider.class.getName()
                        + " updateFactoryConstraints method to support version " + coreVersion.getMajorVersion());
    }

    @Test
    void testStreamReadConstraintsMethods() {
        String message = "There are additional methods in Jackson's StreamReaderConstraints.Builder."
                + " Please update the code in " + DefaultJacksonJaxbJsonProvider.class.getName()
                + " updateFactoryConstraints method";
        Method[] method = StreamReadConstraints.Builder.class.getDeclaredMethods();
        // 2.17 : five setMax... + build() methods
        // 2.18 : six setMax... + build() methods
        Assertions.assertEquals(7, method.length, message);
    }

    @Path("len")
    public static class TestLengthResource {
        @POST
        @Path("number")
        @Produces(MediaType.APPLICATION_JSON)
        public MyEntity number(int len) {
            return new MyEntity(len);
        }

        @POST
        @Path("strlen")
        @Produces(MediaType.APPLICATION_JSON)
        public JsonNode string(int len) {
            return new TextNode(String.valueOf(new MyEntity(len).getValue()));
        }


        @POST
        @Path("entity")
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        public MyEntity number(MyEntity entity) {
            return new MyEntity(4);
        }
    }

    static class MyEntity {

        private int value;

        // For Jackson
        MyEntity() {
        }

        MyEntity(int length) {
            int val = 0;
            for (int i = 1, j = 1; i != length + 1; i++, j++) {
                if (j == 10) {
                    j = 0;
                }
                val = 10 * val + j;
            }
            this.value = val;
        }

        @JsonGetter("value")
        public int getValue() {
            return value;
        }
    }

    static class MyStreamReadConstraintsExceptionMapper implements ExceptionMapper<StreamConstraintsException> {

        @Override
        public Response toResponse(StreamConstraintsException exception) {
            return Response.ok().entity(exception.getMessage()).build();
        }
    }

    static class MyStreamReadConstraints implements ContextResolver<ObjectMapper> {

        @Override
        public ObjectMapper getContext(Class<?> type) {
            final List<Module> modules = ObjectMapper.findModules();
            return new ObjectMapper(JsonFactory.builder().streamReadConstraints(
                    StreamReadConstraints.builder().maxNumberLength(4).build()
            ).build()).registerModules(modules);
        }
    }
}
