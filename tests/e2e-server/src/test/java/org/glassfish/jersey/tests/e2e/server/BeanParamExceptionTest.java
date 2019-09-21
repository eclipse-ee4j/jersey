/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ParamException;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tests the ability to catch WebApplicationException thrown in ParamConverter
 * used along with BeanParam annotation.
 *
 * @author Petr Bouda
 **/
public class BeanParamExceptionTest extends JerseyTest {

    private static final String PARAM_NOT_FOUND = "{\"message\":\"This parameter was not found\",\"status\":400}";

    @Override
    protected Application configure() {
        return new ResourceConfig(
                BeanParamController.class,
                ModelObjectParamConverter.class,
                QueryParamErrorMapper.class,
                JacksonJaxbJsonProvider.class);
    }

    @Path("/")
    @Produces("application/json")
    public static class BeanParamController {

        @GET
        @Path("/query")
        public String queryParam(@QueryParam("queryParam") final ModelObject modelObject) {
            return "Query Param: " + modelObject.toString();
        }

        @GET
        @Path("/bean")
        public String beanParam(@BeanParam final BeanParamObject beanParamObject) {
            return "Bean Param: " + beanParamObject.getModelObject().toString();
        }

    }

    @Provider
    public static class ModelObjectParamConverter implements ParamConverter<ModelObject>, ParamConverterProvider {

        @Override
        public ModelObject fromString(final String s) {
            if ("exception".equalsIgnoreCase(s)) {
                throw new BadParameterException("This parameter was not found");
            }
            return new ModelObject(s);
        }

        @Override
        public String toString(ModelObject modelObject) {
            return modelObject.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> ParamConverter<T> getConverter(Class<T> aClass, Type type, Annotation[] annotations) {
            return aClass.getName().equals(ModelObject.class.getName()) ? (ParamConverter<T>) this : null;
        }

    }

    @Provider
    private static class QueryParamErrorMapper implements ExceptionMapper<ParamException.QueryParamException> {

        @Override
        public Response toResponse(final ParamException.QueryParamException e) {
            Response.Status status = Response.Status.BAD_REQUEST;
            final Throwable cause = e.getCause();
            if (cause instanceof BadParameterException) {
                return Response.status(status).entity(new ErrorMessage(status.getStatusCode(), cause.getMessage())).build();
            }
            return null;
        }
    }

    @Test
    public void testMarshallExceptionQuery() {
        Response response = target().path("query").queryParam("queryParam", "exception")
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        assertEquals(400, response.getStatus());
        assertEquals(PARAM_NOT_FOUND, response.readEntity(String.class));
    }

    @Test
    public void testMarshallExceptionBean() {
        Response response = target().path("bean").queryParam("queryParam", "exception")
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        assertEquals(400, response.getStatus());
        assertEquals(PARAM_NOT_FOUND, response.readEntity(String.class));
    }

    @Test
    public void testMarshallModelQuery() {
        Response response = target().path("query").queryParam("queryParam", "model")
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        assertEquals(200, response.getStatus());
        assertEquals("Query Param: model", response.readEntity(String.class));
    }

    @Test
    public void testMarshallModelBean() {
        Response response = target().path("bean").queryParam("queryParam", "model")
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        assertEquals(200, response.getStatus());
        assertEquals("Bean Param: model", response.readEntity(String.class));
    }

    private static class BadParameterException extends RuntimeException {

        public BadParameterException(final String s) {
            super(s);
        }
    }

    public static class BeanParamObject {

        final ModelObject modelObject;

        public BeanParamObject(@QueryParam("queryParam") final ModelObject modelObject) {
            this.modelObject = modelObject;
        }

        public ModelObject getModelObject() {
            return modelObject;
        }
    }

    public static class ModelObject {

        private final String privateData;

        public ModelObject(final String privateData) {
            this.privateData = privateData;
        }

        @Override
        public String toString() {
            return privateData;
        }
    }

    public static class ErrorMessage {

        private final String message;

        private final int status;

        public ErrorMessage(final int status, final String message) {
            this.message = message;
            this.status = status;
        }

        @JsonProperty
        public String getMessage() {
            return message;
        }

        @JsonProperty
        public int getStatus() {
            return status;
        }
    }

}
