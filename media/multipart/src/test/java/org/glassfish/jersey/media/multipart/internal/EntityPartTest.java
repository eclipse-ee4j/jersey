/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved.
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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityPartTest extends JerseyTest {

    private static final GenericType<List<EntityPart>> LIST_ENTITY_PART_TYPE = new GenericType<List<EntityPart>>(){};
    private static final GenericType<AtomicReference<String>> ATOMIC_REFERENCE_GENERIC_TYPE = new GenericType<>(){};

    @Path("/")
    public static class EntityPartTestResource {
        @GET
        public Response getResponse() throws IOException {
            List<EntityPart> list = new LinkedList<>();
            list.add(EntityPart.withName("part-01").content("TEST1").build());
            list.add(EntityPart.withName("part-02").content("TEST2").build());
            GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
            return Response.ok(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE).build();
        }

        @POST
        @Path("/postList")
        public String postEntityPartList(List<EntityPart> list) throws IOException {
            String entity = list.get(0).getContent(String.class) + list.get(1).getContent(String.class);
            return entity;
        }

        @POST
        @Path("/postForm")
        public String postEntityPartForm(@FormParam("part-01") EntityPart part1, @FormParam("part-02") EntityPart part2)
                throws IOException {
            String entity = part1.getContent(String.class) + part2.getContent(String.class);
            return entity;
        }

        @POST
        @Path("/postListForm")
        public String postEntityPartForm(@FormParam("part-0x") List<EntityPart> part)
                throws IOException {
            String entity = part.get(0).getContent(String.class) + part.get(1).getContent(String.class);
            return entity;
        }

        @POST
        @Path("/postStreams")
        public Response postEntityStreams(@FormParam("name1") EntityPart part1,
                                        @FormParam("name2") EntityPart part2,
                                        @FormParam("name3") EntityPart part3) throws IOException {
            List<EntityPart> list = new LinkedList<>();
            list.add(EntityPart.withName(part1.getName()).fileName(part1.getFileName().get()).content(
                    new ByteArrayInputStream(part1.getContent(String.class).getBytes(StandardCharsets.UTF_8))).build());
            list.add(EntityPart.withName(part2.getName()).fileName(part2.getFileName().get()).content(
                    new ByteArrayInputStream(part2.getContent(String.class).getBytes(StandardCharsets.UTF_8))).build());
            list.add(EntityPart.withName(part3.getName()).fileName(part3.getFileName().get())
                    .content(part3.getContent(StringHolder.class), StringHolder.class)
                    .mediaType(part3.getMediaType()).build());
            GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
            return Response.ok(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE).build();
        }

        @POST
        @Path("/postHeaders")
        public Response postEntityStreams(@FormParam("name1") EntityPart part1) throws IOException {
            List<EntityPart> list = new LinkedList<>();
            list.add(EntityPart.withName(part1.getName()).content(part1.getContent(String.class))
                    .headers(part1.getHeaders()).build());
            GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
            return Response.ok(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE).build();
        }

        @POST
        @Path("/postGeneric")
        public Response postGeneric(@FormParam("name1") EntityPart part1) throws IOException {
            List<EntityPart> list = new LinkedList<>();
            list.add(EntityPart.withName(part1.getName())
                    .content(part1.getContent(ATOMIC_REFERENCE_GENERIC_TYPE), ATOMIC_REFERENCE_GENERIC_TYPE)
                    .mediaType(MediaType.TEXT_PLAIN_TYPE)
                    .build());
            GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
            return Response.ok(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE).build();
        }

        @POST
        @Path("/postFormVarious")
        public Response postFormVarious(@FormParam("name1") EntityPart part1,
                                        @FormParam("name2") InputStream part2,
                                        @FormParam("name3") String part3) throws IOException {
            List<EntityPart> list = new LinkedList<>();
            list.add(EntityPart.withName(part1.getName())
                    .content(part1.getContent(String.class) + new String(part2.readAllBytes()) + part3)
                    .mediaType(MediaType.TEXT_PLAIN_TYPE)
                    .build());
            GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
            return Response.ok(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE).build();
        }

        @GET
        @Produces(MediaType.MULTIPART_FORM_DATA)
        @Path("/getList")
        public List<EntityPart> getList() throws IOException {
            List<EntityPart> list = new LinkedList<>();
            list.add(EntityPart.withName("name1").content("data1").build());
            return list;
        }
    }

    public static class StringHolder extends AtomicReference<String> {
        StringHolder(String name) {
            set(name);
        }
    }

    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public static class StringHolderWorker implements MessageBodyReader<StringHolder>, MessageBodyWriter<StringHolder> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == StringHolder.class;
        }

        @Override
        public StringHolder readFrom(Class<StringHolder> type, Type genericType, Annotation[] annotations,
                                     MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                                     InputStream entityStream) throws IOException, WebApplicationException {
            final StringHolder holder = new StringHolder(new String(entityStream.readAllBytes()));
            return holder;
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return type == StringHolder.class;
        }

        @Override
        public void writeTo(StringHolder s, Class<?> type, Type genericType, Annotation[] annotations,
                            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(s.get().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(EntityPartTestResource.class,
                StringHolderWorker.class, AtomicReferenceProvider.class)
                .property(ServerProperties.WADL_FEATURE_DISABLE, true);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(StringHolderWorker.class).register(AtomicReferenceProvider.class);
    }

    @Test
    public void getEntityPartListTest() throws IOException {
        try (Response response = target().request().get()) {
            List<EntityPart> list = response.readEntity(LIST_ENTITY_PART_TYPE);
            assertEquals("TEST1", list.get(0).getContent(String.class));
            assertEquals("TEST2", list.get(1).getContent(String.class));
        }
    }

    @Test
    public void postEntityPartListTest() throws IOException {
        List<EntityPart> list = new LinkedList<>();
        list.add(EntityPart.withName("part-01").content("TEST").build());
        list.add(EntityPart.withName("part-02").content("1").build());
        GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
        Entity entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE);
        try (Response response = target("/postList").request().post(entity)) {
            assertEquals("TEST1", response.readEntity(String.class));
        }
    }

    @Test
    public void postEntityPartFormParamTest() throws IOException {
        List<EntityPart> list = new LinkedList<>();
        list.add(EntityPart.withName("part-01").content("TEST").build());
        list.add(EntityPart.withName("part-02").content("1").build());
        GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
        Entity entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE);
        try (Response response = target("/postForm").request().post(entity)) {
            assertEquals("TEST1", response.readEntity(String.class));
        }
    }

    @Test
    public void postListEntityPartFormParamTest() throws IOException {
        List<EntityPart> list = new LinkedList<>();
        list.add(EntityPart.withName("part-0x").content("TEST").build());
        list.add(EntityPart.withName("part-0x").content("1").build());
        GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
        Entity entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE);
        try (Response response = target("/postListForm").request().post(entity)) {
            assertEquals("TEST1", response.readEntity(String.class));
        }
    }

    @Test
    public void postEntityPartStreamsTest() throws IOException {
        List<EntityPart> list = new LinkedList<>();
        list.add(EntityPart.withName("name1").fileName("file1.doc").content(
                new ByteArrayInputStream("data1".getBytes(StandardCharsets.UTF_8))).build());
        list.add(EntityPart.withName("name2").fileName("file2.doc").content(
                new ByteArrayInputStream("data2".getBytes(StandardCharsets.UTF_8))).build());
        list.add(EntityPart.withName("name3").fileName("file3.xml")
                .content(new StringHolder("data3"), StringHolder.class)
                .mediaType(MediaType.TEXT_PLAIN_TYPE).build());
        GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {
        };
        Entity entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE);

        try (Response response = target("/postStreams").request().post(entity)) {
            List<EntityPart> result = response.readEntity(LIST_ENTITY_PART_TYPE);

            EntityPart part1 = result.get(0);
            assertEquals("name1", part1.getName());
            assertEquals("file1.doc", part1.getFileName().get());
            assertEquals("data1", part1.getContent(String.class));

            EntityPart part2 = result.get(1);
            assertEquals("name2", part2.getName());
            assertEquals("file2.doc", part2.getFileName().get());
            assertEquals("data2", part2.getContent(String.class));

            EntityPart part3 = result.get(2);
            assertEquals("name3", part3.getName());
            assertEquals("file3.xml", part3.getFileName().get());
            assertEquals("data3", part3.getContent(String.class));
            assertEquals(MediaType.TEXT_PLAIN_TYPE, part3.getMediaType());
        }
    }

    @Test
    public void postHeaderTest() throws IOException {
        List<EntityPart> list = new LinkedList<>();
        list.add(EntityPart.withName("name1").content("data1")
                .header("header-01", "value-01").build());
        GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
        Entity entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE);
        try (Response response = target("/postHeaders").request().post(entity)) {
            List<EntityPart> result = response.readEntity(LIST_ENTITY_PART_TYPE);
            assertEquals("value-01", result.get(0).getHeaders().getFirst("header-01"));
            assertEquals("data1", result.get(0).getContent(String.class));
        }
    }

    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public static class AtomicReferenceProvider implements
            MessageBodyReader<AtomicReference<String>>,
            MessageBodyWriter<AtomicReference<String>> {

        @Override
        public boolean isReadable(Class<?> type, Type generic, Annotation[] annotations, MediaType mediaType) {
            return type == AtomicReference.class
                    && ParameterizedType.class.isInstance(generic)
                    && String.class.isAssignableFrom(ReflectionHelper.getGenericTypeArgumentClasses(generic).get(0));
        }

        @Override
        public AtomicReference<String> readFrom(Class<AtomicReference<String>> type, Type genericType,
                                                Annotation[] annotations, MediaType mediaType,
                                                MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                throws IOException, WebApplicationException {
            return new AtomicReference<String>(new String(entityStream.readAllBytes(), StandardCharsets.UTF_8));
        }

        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return isReadable(type, genericType, annotations, mediaType);
        }

        @Override
        public void writeTo(AtomicReference<String> stringAtomicReference, Class<?> type, Type genericType,
                            Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            entityStream.write(stringAtomicReference.get().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void genericEntityTest() throws IOException {
        List<EntityPart> list = new LinkedList<>();
        list.add(EntityPart.withName("name1")
                .content(new AtomicReference<String>("data1"), ATOMIC_REFERENCE_GENERIC_TYPE)
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .build());
        GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {};
        Entity entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE);
        try (Response response = target("/postGeneric").request().post(entity)) {
            List<EntityPart> result = response.readEntity(LIST_ENTITY_PART_TYPE);
            assertEquals("data1", result.get(0).getContent(String.class));
        }
    }

    @Test
    public void postVariousTest() throws IOException {
        List<EntityPart> list = new LinkedList<>();
        list.add(EntityPart.withName("name1").content("Hello ").build());
        list.add(EntityPart.withName("name2").content("world").build());
        list.add(EntityPart.withName("name3").content("!").build());
        GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(list) {
        };
        Entity entity = Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE);

        try (Response response = target("/postFormVarious").request().post(entity)) {
            List<EntityPart> result = response.readEntity(LIST_ENTITY_PART_TYPE);
            assertEquals("Hello world!", result.get(0).getContent(String.class));
        }
    }

    @Test
    public void getListTest() throws IOException {
        try (Response response = target("/getList").request().get()) {
            List<EntityPart> result = response.readEntity(LIST_ENTITY_PART_TYPE);
            assertEquals("data1", result.get(0).getContent(String.class));
        }
    }
}
