/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exceptions class.
 *
 * @author Santiago.Pericas-Geertsen at oracle.com
 */
public class Exceptions {

    // -- Exceptions
    public static class MyException extends RuntimeException {

        private Response response;

        public MyException(Response response) {
            this.response = response;
        }

        public Response getResponse() {
            return response;
        }
    }

    public static class MySubException extends MyException {

        public MySubException(Response response) {
            super(response);
        }
    }

    public static class MySubSubException extends MySubException {

        public MySubSubException(Response response) {
            super(response);
        }
    }

    // -- Exception Mappers
    @Provider
    public static class MyExceptionMapper implements ExceptionMapper<MyException> {

        @Override
        public Response toResponse(MyException exception) {
            Response r = exception.getResponse();
            return Response.status(r.getStatus()).entity(
                    "Code:" + r.getStatus() + ":" + getClass().getSimpleName()).build();
        }
    }

    @Provider
    public static class MySubExceptionMapper implements ExceptionMapper<MySubException> {

        @Override
        public Response toResponse(MySubException exception) {
            Response r = exception.getResponse();
            return Response.status(r.getStatus()).entity(
                    "Code:" + r.getStatus() + ":" + getClass().getSimpleName()).build();
        }
    }

    @Provider
    public static class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

        @Override
        public Response toResponse(WebApplicationException exception) {
            Response r = exception.getResponse();
            return Response.status(r.getStatus()).entity("Code:" + r.getStatus() + ":"
                    + getClass().getSimpleName()).build();
        }
    }
}
