/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.logging;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HasEntityTimeoutTest {

    private enum DirectionType {
        INBOUND,
        OUTBOUND
    }

    private static class UriInfoHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "getRequestUri":
                    return URI.create("http://localhost:8080/get");
            }
            return null;
        }
    }

    private static class RequestResponseHandler implements InvocationHandler {
        private final DirectionType type;

        private RequestResponseHandler(DirectionType type) {
            this.type = type;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            switch (method.getName()) {
                case "hasEntity":
                    throw new ProcessingException(new SocketTimeoutException("Read timed out"));
                case "getUri":
                    return URI.create("http://localhost:8080");
                case "getStringHeaders":
                case "getHeaders":
                    return new MultivaluedHashMap<String, String>();
                case "getMethod":
                    return "GET";
                case "getMediaType":
                    return MediaType.SERVER_SENT_EVENTS_TYPE;
                case "getEntityStream":
                    return type == DirectionType.OUTBOUND
                            ? new ByteArrayOutputStream()
                            : new ByteArrayInputStream("entity".getBytes());
                case "getStatus":
                    return 200;
                case "getUriInfo":
                    return Proxy.newProxyInstance(
                            UriInfo.class.getClassLoader(),
                            new Class[]{UriInfo.class},
                            new UriInfoHandler());
            }
            return null;
        }
    }

    @Test
    public void testClientFilterTimedOut() throws IOException {
        ClientLoggingFilter loggingFilter = new ClientLoggingFilter(
                LoggingFeature.builder()
                        .withLogger(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME))
                        .level(Level.INFO)
                        .verbosity(LoggingFeature.Verbosity.HEADERS_ONLY)
                        .maxEntitySize(10)
        );

        ClientRequestContext clientRequestContext = (ClientRequestContext) Proxy.newProxyInstance(
                ClientRequestContext.class.getClassLoader(),
                new Class[]{ClientRequestContext.class},
                new RequestResponseHandler(DirectionType.OUTBOUND));
        loggingFilter.filter(clientRequestContext);
    }

    @Test
    public void testClientFilterTimedOutException() throws IOException {
        ClientLoggingFilter loggingFilter = new ClientLoggingFilter(
                LoggingFeature.builder()
                        .withLogger(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME))
                        .level(Level.INFO)
                        .verbosity(LoggingFeature.Verbosity.PAYLOAD_ANY)
                        .maxEntitySize(10)
        );

        ClientRequestContext clientRequestContext = (ClientRequestContext) Proxy.newProxyInstance(
                ClientRequestContext.class.getClassLoader(),
                new Class[]{ClientRequestContext.class},
                new RequestResponseHandler(DirectionType.OUTBOUND));
        try {
            loggingFilter.filter(clientRequestContext);
            throw new RuntimeException("The expected exception has not been thrown");
        } catch (ProcessingException pe) {
            // expected
        }
    }

    @Test
    public void testClientFilterResponseTimedOut() throws IOException {
        ClientLoggingFilter loggingFilter = new ClientLoggingFilter(
                LoggingFeature.builder()
                        .withLogger(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME))
                        .level(Level.INFO)
                        .verbosity(LoggingFeature.Verbosity.HEADERS_ONLY)
                        .maxEntitySize(10)
        );

        ClientRequestContext clientRequestContext = (ClientRequestContext) Proxy.newProxyInstance(
                ClientRequestContext.class.getClassLoader(),
                new Class[]{ClientRequestContext.class},
                new RequestResponseHandler(DirectionType.OUTBOUND));

        ClientResponseContext clientResponseContext = (ClientResponseContext) Proxy.newProxyInstance(
                ClientResponseContext.class.getClassLoader(),
                new Class[]{ClientResponseContext.class},
                new RequestResponseHandler(DirectionType.INBOUND));
        loggingFilter.filter(clientRequestContext, clientResponseContext);
    }

    @Test
    public void testClientFilterResponseTimedOutException() throws IOException {
        ClientLoggingFilter loggingFilter = new ClientLoggingFilter(
                LoggingFeature.builder()
                        .withLogger(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME))
                        .level(Level.INFO)
                        .verbosity(LoggingFeature.Verbosity.PAYLOAD_ANY)
                        .maxEntitySize(10)
        );

        ClientRequestContext clientRequestContext = (ClientRequestContext) Proxy.newProxyInstance(
                ClientRequestContext.class.getClassLoader(),
                new Class[]{ClientRequestContext.class},
                new RequestResponseHandler(DirectionType.OUTBOUND));

        ClientResponseContext clientResponseContext = (ClientResponseContext) Proxy.newProxyInstance(
                ClientResponseContext.class.getClassLoader(),
                new Class[]{ClientResponseContext.class},
                new RequestResponseHandler(DirectionType.INBOUND));

        try {
            loggingFilter.filter(clientRequestContext, clientResponseContext);
            throw new RuntimeException("The expected exception has not been thrown");
        } catch (ProcessingException pe) {
            // expected
        }
    }

    @Test
    public void testServerFilterTimedOut() throws IOException {
        ServerLoggingFilter loggingFilter = new ServerLoggingFilter(
                LoggingFeature.builder()
                        .withLogger(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME))
                        .level(Level.INFO)
                        .verbosity(LoggingFeature.Verbosity.HEADERS_ONLY)
                        .maxEntitySize(10)
        );

        ContainerRequestContext containerRequestContext = (ContainerRequestContext) Proxy.newProxyInstance(
                ContainerRequestContext.class.getClassLoader(),
                new Class[]{ContainerRequestContext.class},
                new RequestResponseHandler(DirectionType.INBOUND));
        loggingFilter.filter(containerRequestContext);
    }

    @Test
    public void testServerFilterTimedOutException() throws IOException {
        ServerLoggingFilter loggingFilter = new ServerLoggingFilter(
                LoggingFeature.builder()
                        .withLogger(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME))
                        .level(Level.INFO)
                        .verbosity(LoggingFeature.Verbosity.PAYLOAD_ANY)
                        .maxEntitySize(10)
        );

        ContainerRequestContext containerRequestContext = (ContainerRequestContext) Proxy.newProxyInstance(
                ContainerRequestContext.class.getClassLoader(),
                new Class[]{ContainerRequestContext.class},
                new RequestResponseHandler(DirectionType.INBOUND));

        try {
            loggingFilter.filter(containerRequestContext);
            throw new RuntimeException("The expected exception has not been thrown");
        } catch (ProcessingException pe) {
            // expected
        }
    }

    @Test
    public void testServerFilterResponseTimedOut() throws IOException {
        ServerLoggingFilter loggingFilter = new ServerLoggingFilter(
                LoggingFeature.builder()
                        .withLogger(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME))
                        .level(Level.INFO)
                        .verbosity(LoggingFeature.Verbosity.HEADERS_ONLY)
                        .maxEntitySize(10)
        );

        ContainerRequestContext containerRequestContext = (ContainerRequestContext) Proxy.newProxyInstance(
                ContainerRequestContext.class.getClassLoader(),
                new Class[]{ContainerRequestContext.class},
                new RequestResponseHandler(DirectionType.INBOUND));

        ContainerResponseContext containerResponseContext = (ContainerResponseContext) Proxy.newProxyInstance(
                ContainerResponseContext.class.getClassLoader(),
                new Class[]{ContainerResponseContext.class},
                new RequestResponseHandler(DirectionType.OUTBOUND));

        loggingFilter.filter(containerRequestContext, containerResponseContext);
    }

    @Test
    public void testServerFilterResponseTimedOutException() throws IOException {
        ServerLoggingFilter loggingFilter = new ServerLoggingFilter(
                LoggingFeature.builder()
                        .withLogger(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME))
                        .level(Level.INFO)
                        .verbosity(LoggingFeature.Verbosity.PAYLOAD_ANY)
                        .maxEntitySize(10)
        );

        ContainerRequestContext containerRequestContext = (ContainerRequestContext) Proxy.newProxyInstance(
                ContainerRequestContext.class.getClassLoader(),
                new Class[]{ContainerRequestContext.class},
                new RequestResponseHandler(DirectionType.INBOUND));

        ContainerResponseContext containerResponseContext = (ContainerResponseContext) Proxy.newProxyInstance(
                ContainerResponseContext.class.getClassLoader(),
                new Class[]{ContainerResponseContext.class},
                new RequestResponseHandler(DirectionType.OUTBOUND));

        try {
            loggingFilter.filter(containerRequestContext, containerResponseContext);
            throw new RuntimeException("The expected exception has not been thrown");
        } catch (ProcessingException pe) {
            // expected
        }
    }

}
