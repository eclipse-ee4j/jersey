/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case for unsupported media type.
 *
 * @author Miroslav Fuksa
 */
public class MessageBodyReaderUnsupportedTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        // TestEntityProvider must not be registered in the application for this test case.
        return new ResourceConfig(Resource.class);
    }

    /**
     * Send request to with application/json content to server where JsonJaxbBinder is not registered. UNSUPPORTED_MEDIA_TYPE
     * should be returned.
     */
    @Test
    public void testUnsupportedMessageBodyReader() {
        client().register(new TestEntityProvider());
        TestEntity entity = new TestEntity("testEntity");
        Response response = target().path("test").request(TestEntityProvider.TEST_ENTITY_TYPE)
                .post(Entity.entity(entity, TestEntityProvider.TEST_ENTITY_TYPE));

        // TestEntityProvider is not registered on the server and therefore the server should return UNSUPPORTED_MEDIA_TYPE
        assertEquals(Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
        assertFalse(Resource.methodCalled);
        String responseEntity = response.readEntity(String.class);
        assertTrue((responseEntity == null) || (responseEntity.length() == 0));
    }

    /**
     * Test Resource class.
     *
     * @author Miroslav Fuksa
     */
    @Path("test")
    public static class Resource {

        private static volatile boolean methodCalled;

        /**
         * Resource method producing a {@code null} result.
         *
         * @param entity test entity.
         * @return {@code null}.
         */
        @POST
        @Produces(TestEntityProvider.TEST_ENTITY)
        @Consumes(TestEntityProvider.TEST_ENTITY)
        @SuppressWarnings("UnusedParameters")
        public TestEntity processEntityAndProduceNull(TestEntity entity) {
            methodCalled = true;
            return null;
        }
    }

    /**
     * Test bean.
     *
     * @author Miroslav Fuksa
     */
    public static class TestEntity {

        private final String value;

        /**
         * Get value.
         *
         * @return value.
         */
        public String getValue() {
            return value;
        }

        /**
         * Create new test entity.
         *
         * @param value entity value.
         */
        public TestEntity(String value) {
            super();
            this.value = value;
        }
    }

    /**
     * Custom test entity provider.
     */
    @Produces("test/entity")
    @Consumes("test/entity")
    public static class TestEntityProvider implements MessageBodyReader<TestEntity>, MessageBodyWriter<TestEntity> {
        /**
         * Test bean media type string.
         */
        public static final String TEST_ENTITY = "test/entity";
        /**
         * Test bean media type.
         */
        public static final MediaType TEST_ENTITY_TYPE = MediaType.valueOf(TEST_ENTITY);

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return TestEntity.class == type && TEST_ENTITY_TYPE.equals(mediaType);
        }

        @Override
        public TestEntity readFrom(Class<TestEntity> type,
                                   Type genericType,
                                   Annotation[] annotations,
                                   MediaType mediaType,
                                   MultivaluedMap<String, String> httpHeaders,
                                   InputStream entityStream) throws IOException, WebApplicationException {
            return new TestEntity(ReaderWriter.readFromAsString(entityStream, mediaType));
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return TestEntity.class == type && TEST_ENTITY_TYPE.equals(mediaType);
        }

        @Override
        public long getSize(TestEntity testEntity,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(TestEntity testEntity,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            ReaderWriter.writeToAsString(testEntity.getValue(), entityStream, mediaType);
        }
    }

}
