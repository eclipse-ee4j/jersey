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

package org.glassfish.jersey.server.internal.inject;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ParamException;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Not sure whether this is relevant anymore.
 *
 * @author Paul Sandoz
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@SuppressWarnings("unchecked")
public class ParamExceptionMappingTest extends AbstractTest {

    public abstract static class BaseExceptionMapper<T extends ParamException> implements ExceptionMapper<T> {

        public Response toResponse(T exception, String entity) {
            assertEquals("x", exception.getParameterName());

            // path param and form param can be integers in this test, thus different default value
            if (!exception.getParameterType().equals(PathParam.class)
                    && !exception.getParameterType().equals(FormParam.class)) {

                assertEquals("default", exception.getDefaultStringValue());
            } else {
                assertTrue(exception.getDefaultStringValue().equals("default") || exception.getDefaultStringValue().equals("1"));
            }
            return Response.fromResponse(exception.getResponse()).entity(entity).build();
        }
    }

    public static class ParamExceptionMapper extends BaseExceptionMapper<ParamException> {

        public Response toResponse(ParamException exception) {
            return toResponse(exception, "param");
        }
    }

    public static class UriExceptionMapper extends BaseExceptionMapper<ParamException.UriParamException> {

        public Response toResponse(ParamException.UriParamException exception) {
            return toResponse(exception, "uri");
        }
    }

    public static class PathExceptionMapper extends BaseExceptionMapper<ParamException.PathParamException> {

        public Response toResponse(ParamException.PathParamException exception) {
            return toResponse(exception, "path");
        }
    }

    public static class MatrixExceptionMapper extends BaseExceptionMapper<ParamException.MatrixParamException> {

        public Response toResponse(ParamException.MatrixParamException exception) {
            return toResponse(exception, "matrix");
        }
    }

    public static class QueryExceptionMapper extends BaseExceptionMapper<ParamException.QueryParamException> {

        public Response toResponse(ParamException.QueryParamException exception) {
            return toResponse(exception, "query");
        }
    }

    public static class
    CookieExceptionMapper extends BaseExceptionMapper<ParamException.CookieParamException> {

        public Response toResponse(ParamException.CookieParamException exception) {
            return toResponse(exception, "cookie");
        }
    }

    public static class HeaderExceptionMapper extends BaseExceptionMapper<ParamException.HeaderParamException> {

        public Response toResponse(ParamException.HeaderParamException exception) {
            return toResponse(exception, "header");
        }
    }

    public static class FormExceptionMapper extends BaseExceptionMapper<ParamException.FormParamException> {

        public Response toResponse(ParamException.FormParamException exception) {
            return toResponse(exception, "form");
        }
    }

    @Path("/")
    public static class ParamExceptionMapperResource {

        @Path("path/{x}")
        @GET
        public String getPath(@DefaultValue("1") @PathParam("x") int x) {
            return "";
        }

        @Path("matrix")
        @GET
        public String getMatrix(@DefaultValue("default") @MatrixParam("x") URI x) {
            return "";
        }

        @Path("query")
        @GET
        public String getQuery(@DefaultValue("default") @QueryParam("x") URI x) {
            return "";
        }

        @Path("cookie")
        @GET
        public String getCookie(@DefaultValue("default") @CookieParam("x") URI x) {
            return "";
        }

        @Path("header")
        @GET
        public String getHeader(@DefaultValue("default") @HeaderParam("x") URI x) {
            return "";
        }

        @Path("form")
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public String postForm(@DefaultValue("default") @FormParam("x") URI x) {
            return "";
        }

        @Path("form-int")
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public String postForm(@DefaultValue("1") @FormParam("x") int x) {
            return "";
        }
    }

    @Test
    public void testParamException() throws ExecutionException, InterruptedException {
        initiateWebApplication(ParamExceptionMapperResource.class,
                               PathExceptionMapper.class,
                               MatrixExceptionMapper.class,
                               QueryExceptionMapper.class,
                               CookieExceptionMapper.class,
                               HeaderExceptionMapper.class,
                               FormExceptionMapper.class);

        ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/").path("path/ test").build().toString());
        assertEquals("path", responseContext.getEntity());

        responseContext = getResponseContext(UriBuilder.fromPath("/").path("matrix;x= 123").build().toString());
        assertEquals("matrix", responseContext.getEntity());

        responseContext = getResponseContext(UriBuilder.fromPath("/").path("query").queryParam("x", " 123").build().toString());
        assertEquals("query", responseContext.getEntity());

        responseContext = getResponseContext(UriBuilder.fromPath("/").path("cookie").build().toString(), new Cookie("x", " 123"));
        assertEquals("cookie", responseContext.getEntity());

        responseContext = apply(
                RequestContextBuilder.from("/header", "GET")
                        .header("x", " 123")
                        .build()
        );
        assertEquals("header", responseContext.getEntity());

        Form f = new Form();
        f.param("x", " 123");
        responseContext = apply(
                RequestContextBuilder.from("/form", "POST")
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .entity(f)
                        .build()
        );
        assertEquals("form", responseContext.getEntity());
    }

    @Test
    public void testFormParamPrimitiveValidation() throws ExecutionException, InterruptedException {
        initiateWebApplication(ParamExceptionMapperResource.class,
                               FormExceptionMapper.class);

        Form f = new Form();
        f.param("x", "http://oracle.com");
        ContainerResponseContext responseContext = apply(
                RequestContextBuilder.from("/form-int", "POST")
                                     .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                                     .entity(f)
                                     .build()
        );

        assertEquals("form", responseContext.getEntity());
    }

    @Test
    public void testGeneralParamException() throws ExecutionException, InterruptedException {
        initiateWebApplication(ParamExceptionMapperResource.class,
                ParamExceptionMapper.class);

        ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/").path("path/ 123").build().toString());
        assertEquals("param", responseContext.getEntity());

        responseContext = getResponseContext(UriBuilder.fromPath("/").path("matrix;x= 123").build().toString());
        assertEquals("param", responseContext.getEntity());

        responseContext = getResponseContext(UriBuilder.fromPath("/").path("query").queryParam("x", " 123").build().toString());
        assertEquals("param", responseContext.getEntity());

        responseContext = getResponseContext(UriBuilder.fromPath("/").path("cookie").build().toString(), new Cookie("x", " 123"));
        assertEquals("param", responseContext.getEntity());

        responseContext = apply(
                RequestContextBuilder.from("/header", "GET")
                        .header("x", " 123")
                        .build()
        );
        assertEquals("param", responseContext.getEntity());

        Form f = new Form();
        f.param("x", " 123");
        responseContext = apply(
                RequestContextBuilder.from("/form", "POST")
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .entity(f)
                        .build()
        );
        assertEquals("param", responseContext.getEntity());
    }

    @Test
    public void testURIParamException() throws ExecutionException, InterruptedException {
        initiateWebApplication(ParamExceptionMapperResource.class,
                UriExceptionMapper.class);

        ContainerResponse responseContext = getResponseContext(UriBuilder.fromPath("/").path("path/ 123").build().toString());
        assertEquals("uri", responseContext.getEntity());

        responseContext = getResponseContext(UriBuilder.fromPath("/").path("matrix;x= 123").build().toString());
        assertEquals("uri", responseContext.getEntity());

        responseContext = getResponseContext(UriBuilder.fromPath("/").path("query").queryParam("x", " 123").build().toString());
        assertEquals("uri", responseContext.getEntity());
    }

}
