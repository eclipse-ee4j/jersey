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

package org.glassfish.jersey.tests.integration.jersey1107;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * @author Michal Gajdos
 */
public class Jersey1107 extends Application {

    /**
     * This MessageBodyWriter does not support the "exception/nullpointerexception" media type required by the
     * {@code Resource#getNpe()} method which should result in an empty {@code MessageBodyWriter} and therefore
     * an NPE in {@code ApplicationHandler}.
     *
     * @see org.glassfish.jersey.tests.integration.jersey1107.Jersey1107.Resource#getNpe()
     */
    @Provider
    @Produces({"exception/ioexception", "exception/webapplicationexception"})
    public static class ExceptionThrower implements MessageBodyWriter<Exception> {

        @Override
        public boolean isWriteable(
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType) {
            return IOException.class.isAssignableFrom(type) || RuntimeException.class.isAssignableFrom(type);
        }

        @Override
        public long getSize(
                Exception t,
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(
                Exception e,
                Class<?> type,
                Type genericType,
                Annotation[] annotations,
                MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException, WebApplicationException {
            // Cannot write anything into the entityStream to ensure the ContainerResponseWriter#writeResponseStatusAndHeaders
            // in ApplicationHandler#writeResponse is not invoked.

            // Simply throw the given exception.
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw (RuntimeException) e;
            }
        }

    }

    @Path("/")
    public static class Resource {

        @GET
        @Path("/ioe")
        @Produces("exception/ioexception")
        public IOException getIoe() {
            return new IOException();
        }

        @GET
        @Path("/wae")
        @Produces("exception/webapplicationexception")
        public WebApplicationException getWae() {
            return new WebApplicationException();
        }

        @GET
        @Path("/npe")
        @Produces("exception/nullpointerexception")
        public NullPointerException getNpe() {
            return new NullPointerException("This message should never get to the client!");
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>() {{
            add(Resource.class);
            add(ExceptionThrower.class);
        }};
    }

}
