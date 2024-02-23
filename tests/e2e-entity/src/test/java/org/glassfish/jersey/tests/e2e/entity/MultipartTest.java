/*
 * Copyright (c) 2012, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jsonb.JsonBindingFeature;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Martin Matula
 */
public class MultipartTest {

    private static Class<? extends Feature> featureClass;

    public static class MyObject {

        private String value;

        public MyObject() {
        }

        public MyObject(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }
    }

    @Path("/")
    public static class MultipartResource {

        @POST
        @Path("filename")
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        @Produces(MediaType.TEXT_PLAIN)
        public String filename(final FormDataMultiPart entity) {
            return entity.getField("text").getValue() + entity.getField("file").getContentDisposition().getFileName();
        }

        @POST
        @Path("mbr")
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        @Produces(MediaType.TEXT_PLAIN)
        public Response mbr(final FormDataMultiPart entity) {
            entity.getField("text").getValueAs(MultipartResource.class);

            return Response.ok("ko").build();
        }

        @POST
        @Path("listAsParameter")
        @Consumes(MediaType.MULTIPART_FORM_DATA)
        public String process(@FormDataParam(value = "object") final MyObject object,
                              @FormDataParam(value = "list") final List<MyObject> list) {
            String value = object.value;

            for (final MyObject obj : list) {
                value += "_" + obj.value;
            }

            return value;
        }
    }

    public static class MessageBodyProvider
            implements MessageBodyReader<MultipartResource>, MessageBodyWriter<MultipartResource> {

        @Override
        public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                  final MediaType mediaType) {
            return true;
        }

        @Override
        public MultipartResource readFrom(final Class<MultipartResource> type, final Type genericType,
                                          final Annotation[] annotations, final MediaType mediaType,
                                          final MultivaluedMap<String, String> httpHeaders,
                                          final InputStream entityStream) throws IOException, WebApplicationException {
            throw new IOException("IOE");
        }

        @Override
        public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                                   final MediaType mediaType) {
            return true;
        }

        @Override
        public long getSize(final MultipartResource multipartResource, final Class<?> type, final Type genericType,
                            final Annotation[] annotations, final MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(final MultipartResource multipartResource, final Class<?> type, final Type genericType,
                            final Annotation[] annotations, final MediaType mediaType,
                            final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
                throws IOException, WebApplicationException {

            throw new IOException("IOE");
        }
    }

    private static final List<Class<? extends Feature>> providers =
            Arrays.asList(MoxyJsonFeature.class, JacksonFeature.class, JsonBindingFeature.class);

    protected static Application configure(Class<? extends Feature> featureClass) {
        MultipartTest.featureClass = featureClass;
        return new ResourceConfig(MultipartResource.class, MessageBodyProvider.class)
                .register(MultiPartFeature.class)
                .register(featureClass);
//                .register(JacksonFeature.class);
//                .register(JsonBindingFeature.class);
    }
    public static Stream<Class<? extends Feature>> parameters() {
        return providers.stream();
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        final Collection<DynamicContainer> tests = new ArrayList<>();
        MultipartTest.parameters().forEach(feature -> {
            final MultipartTest.MultipartTemplateTest test =
                    new MultipartTemplateTest(feature) {};
            tests.add(TestHelper.toTestContainer(test, feature.getClass().getSimpleName()));
        });
        return tests;
    }
    public abstract class MultipartTemplateTest extends JerseyTest {

        public MultipartTemplateTest(Class<? extends Feature> featureProvider) {
            super(MultipartTest.configure(featureProvider));
        }
        @Override
        protected void configureClient(final ClientConfig config) {
            config.register(MultiPartFeature.class);
            config.register(featureClass);
            //config.register(JacksonFeature.class);
            //config.register(JsonBindingFeature.class);
        }

        @Test
        public void testFileNameInternetExplorer() throws Exception {
            final InputStream entity = getClass().getResourceAsStream("multipart-testcase.txt");
            final Response response = target("filename")
                    .request()
                    .header("User-Agent", "Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)")
                    .post(Entity.entity(entity, "multipart/form-data; boundary=---------------------------7dc941520888"));

            assertThat(response.readEntity(String.class), equalTo("bhhklbpom.xml"));
        }

        @Test
        public void testFileName() throws Exception {
            final InputStream entity = getClass().getResourceAsStream("multipart-testcase.txt");
            final Response response = target("filename")
                    .request()
                    .post(Entity.entity(entity, "multipart/form-data; boundary=---------------------------7dc941520888"));

            assertThat(response.readEntity(String.class), equalTo("bhhklbC:javaprojectsmultipart-testcasepom.xml"));
        }

        @Test
        public void testMbrExceptionServer() throws Exception {
            final InputStream entity = getClass().getResourceAsStream("multipart-testcase.txt");
            final Response response = target("mbr")
                    .request()
                    .post(Entity.entity(entity, "multipart/form-data; boundary=---------------------------7dc941520888"));

            assertThat(response.getStatus(), equalTo(500));
        }

        /**
         * Test that injection of a list (specific type) works.
         */
        @Test
        public void testSpecificListAsParameter() throws Exception {
            if (featureClass == MoxyJsonFeature.class) {
                // No available MessageBodyWriter for class "class java.util.Arrays$ArrayList" and media type "application/json"
                return;
            }
            final MyObject object = new MyObject("object");
            final List<MyObject> list = Arrays.asList(new MyObject("list1"), new MyObject("list2"));

            final FormDataMultiPart mp = new FormDataMultiPart();
            mp.bodyPart(new FormDataBodyPart(FormDataContentDisposition.name("object").fileName("object").build(),
                    object, MediaType.APPLICATION_JSON_TYPE));
            mp.bodyPart(new FormDataBodyPart(FormDataContentDisposition.name("list").fileName("list").build(),
                    list, MediaType.APPLICATION_JSON_TYPE));

            final Response response = target("listAsParameter")
                    .request().post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA_TYPE));

            assertThat(response.readEntity(String.class), is("object_list1_list2"));
        }

        @Test
        public void testEmptyEntity() throws Exception {
            final Response response = target("filename")
                    .request()
                    .post(Entity.entity(null, MediaType.MULTIPART_FORM_DATA_TYPE));

            assertThat(response.getStatus(), is(400));
        }

        @Test
        public void testEmptyEntityWithoutContentType() throws Exception {
            final Response response = target("filename")
                    .request()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_TYPE)
                    .post(null);

            assertThat(response.getStatus(), is(400));
        }
    }
}
