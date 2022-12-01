/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

public class OptionalParamConverterTest extends JerseyTest {

    private static final String PARAM_NAME = "paramName";

    @Path("/IntegerResource")
    public static class IntegerResource {
        @GET
        @Path("/fromInteger")
        public Response fromInteger(@QueryParam(PARAM_NAME) Integer data) {
            return Response.ok(0).build();
        }
        @GET
        @Path("/fromIntegerNotNull")
        public Response fromIntegerNotNull(@NotNull @QueryParam(PARAM_NAME) Integer data) {
            return Response.ok(0).build();
        }
    }

    @Path("/OptionalResource")
    public static class OptionalResource {

        @GET
        @Path("/fromString")
        public Response fromString(@QueryParam(PARAM_NAME) Optional<String> data) {
            return Response.ok(data.orElse("")).build();
        }

        @GET
        @Path("/fromInteger")
        public Response fromInteger(@QueryParam(PARAM_NAME) Optional<Integer> data) {
            return Response.ok(data.orElse(0)).build();
        }

        @GET
        @Path("/fromIntegerNotNull")
        public Response fromIntegerNotNull(@NotNull @QueryParam(PARAM_NAME) Optional<Integer> data) {
            return Response.ok(data.orElse(0)).build();
        }

        @GET
        @Path("/fromDate")
        public Response fromDate(@QueryParam(PARAM_NAME) Optional<Date> data) throws ParseException {
            return Response.ok(data.orElse(new Date(1609459200000L))).build();
        }

        @GET
        @Path("/fromInstant")
        public Response fromInstant(@QueryParam(PARAM_NAME) Optional<Instant> data) {
            return Response.ok(data.orElse(Instant.parse("2021-01-01T00:00:00Z")).toString()).build();
        }

        @GET
        @Path("/fromList")
        public Response fromList(@QueryParam(PARAM_NAME) List<Optional<Integer>> data) {
            StringBuilder builder = new StringBuilder("");
            for (Optional<Integer> val : data) {
                builder.append(val.orElse(0));
            }
            return Response.ok(builder.toString()).build();
        }
    }

    @Provider
    public static class InstantParamConverterProvider implements ParamConverterProvider {
        @Override
        public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
            if (rawType.equals(Instant.class)) {
                return new ParamConverter<T>() {
                    @Override
                    public T fromString(String value) {
                        if (value == null) {
                            throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
                        }
                        try {
                            return rawType.cast(Instant.parse(value));
                        } catch (Exception e) {
                            throw new ExtractorException(e);
                        }
                    }

                    @Override
                    public String toString(T value) {
                        if (value == null) {
                            throw new IllegalArgumentException();
                        }
                        return value.toString();
                    }
                };
            } else {
                return null;
            }
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(OptionalResource.class, IntegerResource.class, InstantParamConverterProvider.class);
    }

    @Test
    public void fromOptionalStr() {
        Response empty = target("/OptionalResource/fromString").request().get();
        Response notEmpty = target("/OptionalResource/fromString").queryParam(PARAM_NAME, "anyValue").request().get();
        assertEquals(200, empty.getStatus());
        assertEquals("", empty.readEntity(String.class));
        assertEquals(200, notEmpty.getStatus());
        assertEquals("anyValue", notEmpty.readEntity(String.class));
    }

    @Test
    public void fromOptionalInteger() {
        Response missing = target("/OptionalResource/fromInteger").request().get();
        Response empty = target("/OptionalResource/fromInteger").queryParam(PARAM_NAME, "").request().get();
        Response notEmpty = target("/OptionalResource/fromInteger").queryParam(PARAM_NAME, 1).request().get();
        Response invalid = target("/OptionalResource/fromInteger").queryParam(PARAM_NAME, "invalid").request().get();
        Response missingNotNull = target("/OptionalResource/fromIntegerNotNull").request().get();
        assertEquals(200, missing.getStatus());
        assertEquals(Integer.valueOf(0), missing.readEntity(Integer.class));
        assertEquals(200, empty.getStatus());
        assertEquals(Integer.valueOf(0), empty.readEntity(Integer.class));
        assertEquals(200, notEmpty.getStatus());
        assertEquals(Integer.valueOf(1), notEmpty.readEntity(Integer.class));
        assertEquals(404, invalid.getStatus());
        assertFalse(invalid.hasEntity());
        assertEquals(200, missingNotNull.getStatus());
        assertEquals(Integer.valueOf(0), missingNotNull.readEntity(Integer.class));
    }

    @Test
    public void fromInteger() {
        Response missing = target("/IntegerResource/fromInteger").request().get();
        Response missingNotNull = target("/IntegerResource/fromIntegerNotNull").request().get();
        assertEquals(200, missing.getStatus());
        assertEquals(Integer.valueOf(0), missing.readEntity(Integer.class));
        assertEquals(400, missingNotNull.getStatus());
    }

    @Test
    public void fromOptionalDate() {
        Response missing = target("/OptionalResource/fromDate").request().get();
        Response empty = target("/OptionalResource/fromDate").queryParam(PARAM_NAME, "").request().get();
        Response notEmpty = target("/OptionalResource/fromDate").queryParam(PARAM_NAME, "Sat, 01 May 2021 12:00:00 GMT")
                .request().get();
        Response invalid = target("/OptionalResource/fromDate").queryParam(PARAM_NAME, "invalid").request().get();
        assertEquals(200, missing.getStatus());
        assertEquals(new Date(1609459200000L), missing.readEntity(Date.class));
        assertEquals(200, empty.getStatus());
        assertEquals(new Date(1609459200000L), empty.readEntity(Date.class));
        assertEquals(200, notEmpty.getStatus());
        assertEquals(new Date(1619870400000L), notEmpty.readEntity(Date.class));
        assertEquals(404, invalid.getStatus());
        assertFalse(invalid.hasEntity());
    }

    @Test
    public void fromOptionalInstant() {
        Response missing = target("/OptionalResource/fromInstant").request().get();
        Response empty = target("/OptionalResource/fromInstant").queryParam(PARAM_NAME, "").request().get();
        Response notEmpty = target("/OptionalResource/fromInstant").queryParam(PARAM_NAME, "2021-05-01T12:00:00Z")
                .request().get();
        Response invalid = target("/OptionalResource/fromInstant").queryParam(PARAM_NAME, "invalid").request().get();
        assertEquals(200, missing.getStatus());
        assertEquals("2021-01-01T00:00:00Z", missing.readEntity(String.class));
        assertEquals(200, empty.getStatus());
        assertEquals("2021-01-01T00:00:00Z", empty.readEntity(String.class));
        assertEquals(200, notEmpty.getStatus());
        assertEquals("2021-05-01T12:00:00Z", notEmpty.readEntity(String.class));
        assertEquals(404, invalid.getStatus());
        assertFalse(invalid.hasEntity());
    }

    @Test
    public void fromOptionalList() {
        Response missing = target("/OptionalResource/fromList").request().get();
        Response empty = target("/OptionalResource/fromList")
                .queryParam(PARAM_NAME, "").request().get();
        Response partiallyEmpty = target("/OptionalResource/fromList")
                .queryParam(PARAM_NAME, 1)
                .queryParam(PARAM_NAME, "").request().get();
        Response invalid = target("/OptionalResource/fromList")
                .queryParam(PARAM_NAME, "invalid").request().get();
        Response partiallyInvalid = target("/OptionalResource/fromList")
                .queryParam(PARAM_NAME, 1)
                .queryParam(PARAM_NAME, "invalid").request().get();
        Response notEmpty = target("/OptionalResource/fromList")
                .queryParam(PARAM_NAME, 1)
                .queryParam(PARAM_NAME, 2).request().get();
        assertEquals(200, missing.getStatus());
        assertEquals("", missing.readEntity(String.class));
        assertEquals(200, empty.getStatus());
        assertEquals("0", empty.readEntity(String.class));
        assertEquals(200, partiallyEmpty.getStatus());
        assertEquals("10", partiallyEmpty.readEntity(String.class));
        assertEquals(404, invalid.getStatus());
        assertFalse(invalid.hasEntity());
        assertEquals(404, partiallyInvalid.getStatus());
        assertFalse(partiallyInvalid.hasEntity());
        assertEquals(200, notEmpty.getStatus());
        assertEquals("12", notEmpty.readEntity(String.class));
    }
}
