/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.annotation.Priority;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * JERSEY-2206 reproducer
 *
 * @author Libor Kramolis
 */
public class RequestHeaderModificationsTest {

    private static final Logger LOGGER = Logger.getLogger(RequestHeaderModificationsTest.class.getName());
    private static final boolean GZIP = false; // change to true when JERSEY-2341 fixed
    private static final boolean DUMP_ENTITY = false; // I have troubles to dump entity with async jetty!

    private static final String QUESTION = "QUESTION";
    private static final String ANSWER = "ANSWER";
    private static final String REQUEST_HEADER_NAME_CLIENT = "Client-Prop";
    private static final String REQUEST_HEADER_VALUE_CLIENT = "Client-Value";
    private static final String REQUEST_HEADER_NAME_FILTER = "Filter-Prop";
    private static final String REQUEST_HEADER_VALUE_FILTER = "Filter-Value";
    private static final String REQUEST_HEADER_NAME_INTERCEPTOR = "Iceptor-Prop";
    private static final String REQUEST_HEADER_VALUE_INTERCEPTOR = "Iceptor-Value";
    private static final String REQUEST_HEADER_NAME_MBW = "Mbw-Prop";
    private static final String REQUEST_HEADER_VALUE_MBW = "Mbw-Value";
    private static final String REQUEST_HEADER_MODIFICATION_SUPPORTED = "modificationSupported";
    private static final String PATH = "/resource";

    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {new HttpUrlConnectorProvider(), true, true, false, false},
                {new GrizzlyConnectorProvider(), false, false, false, false}, // change to true when JERSEY-2341 fixed
                {new JettyConnectorProvider(), true, false, false, false}, // change to true when JERSEY-2341 fixed
                {new ApacheConnectorProvider(), false, false, false, false}, // change to true when JERSEY-2341 fixed
                {new Apache5ConnectorProvider(), false, false, false, false}, // change to true when JERSEY-2341 fixed
                {new HttpUrlConnectorProvider(), true, true, true, true},
                {new GrizzlyConnectorProvider(), false, false, true, true}, // change to true when JERSEY-2341 fixed
                {new JettyConnectorProvider(), true, false, true, false}, // change to true when JERSEY-2341 fixed
                {new ApacheConnectorProvider(), false, false, true, true}, // change to true when JERSEY-2341 fixed
                {new Apache5ConnectorProvider(), false, false, true, true}, // change to true when JERSEY-2341 fixed
        });
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        testData().forEach(arr -> {
            RequestHeaderModificationsTemplateTest test = new RequestHeaderModificationsTemplateTest(
                    (ConnectorProvider) arr[0], (boolean) arr[1], (boolean) arr[2], (boolean) arr[3], (boolean) arr[4]) {};
            tests.add(TestHelper.toTestContainer(test, String.format("%s (%s, %s, %s)",
                    RequestHeaderModificationsTemplateTest.class.getSimpleName(),
                    arr[0].getClass().getSimpleName(), arr[1], arr[2])));
        });
        return tests;
    }

    public abstract static class RequestHeaderModificationsTemplateTest extends JerseyTest {
        private final ConnectorProvider connectorProvider;
        private final boolean modificationSupported; // remove when JERSEY-2341 fixed
        private final boolean modificationSupportedAsync; // remove when JERSEY-2341 fixed

        private final boolean addHeader;
        private final boolean addHeaderAsync;

        public RequestHeaderModificationsTemplateTest(ConnectorProvider connectorProvider,
                                                      boolean modificationSupported,
                                                      boolean modificationSupportedAsync,
                                                      boolean addHeader,
                                                      boolean addHeaderAsync) {
            this.connectorProvider = connectorProvider;
            this.modificationSupported = modificationSupported;
            this.modificationSupportedAsync = modificationSupportedAsync;
            this.addHeader = addHeader;
            this.addHeaderAsync = addHeaderAsync;
        }

        @Override
        protected Application configure() {
            set(TestProperties.RECORD_LOG_LEVEL, Level.WARNING.intValue());

            enable(TestProperties.LOG_TRAFFIC);
            if (DUMP_ENTITY) {
                enable(TestProperties.DUMP_ENTITY);
            }
            return new ResourceConfig(TestResource.class)
                    .register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.HEADERS_ONLY));
        }

        @Override
        protected void configureClient(ClientConfig clientConfig) {
            clientConfig.register(MyClientRequestFilter.class);
            clientConfig.connectorProvider(connectorProvider);
        }

        @Test
        public void testWarningLogged() throws Exception {
            Response response = requestBuilder(addHeader).post(requestEntity());
            assertResponse(response, modificationSupported, addHeader);
        }

        @Test
        public void testWarningLoggedAsync() throws Exception {
            AsyncInvoker asyncInvoker = requestBuilder(addHeaderAsync).async();
            Future<Response> responseFuture = asyncInvoker.post(requestEntity());
            Response response = responseFuture.get();
            assertResponse(response, modificationSupportedAsync, addHeaderAsync);
        }

        private Invocation.Builder requestBuilder(boolean addHeader) {
            return target(PATH)
                    .register(new MyWriterInterceptor(addHeader))
                    .register(new MyMessageBodyWriter(addHeader))
                    .request()
                    .header(REQUEST_HEADER_NAME_CLIENT, REQUEST_HEADER_VALUE_CLIENT)
                    .header(REQUEST_HEADER_MODIFICATION_SUPPORTED, modificationSupported && addHeader)
                    .header("hello", "double").header("hello", "value");
        }

        private Entity<MyEntity> requestEntity() {
            return Entity.text(new MyEntity(QUESTION));
        }

        private void assertResponse(Response response, boolean modificationSupported, boolean addHeader) {
            if (!modificationSupported) {
                final String UNSENT_HEADER_CHANGES = "Unsent header changes";
                LogRecord logRecord = findLogRecord(UNSENT_HEADER_CHANGES);
                if (addHeader) {
                    assertNotNull(logRecord, "Missing LogRecord for message '" + UNSENT_HEADER_CHANGES + "'.");
                    assertThat(logRecord.getMessage(), containsString(REQUEST_HEADER_NAME_INTERCEPTOR));
                    assertThat(logRecord.getMessage(), containsString(REQUEST_HEADER_NAME_MBW));
                } else {
                    assertNull(logRecord, "Unexpected LogRecord for message '" + UNSENT_HEADER_CHANGES + "'.");
                }
            }

            assertEquals(200, response.getStatus());
            assertEquals(ANSWER, response.readEntity(String.class));
        }

        private LogRecord findLogRecord(String messageContains) {
            for (final LogRecord record : getLoggedRecords()) {
                if (record.getMessage().contains(messageContains)) {
                    return record;
                }
            }
            return null;
        }
    }

    @Path(PATH)
    public static class TestResource {

        @POST
        public String handle(InputStream questionStream,
                             @HeaderParam(REQUEST_HEADER_NAME_CLIENT) String client,
                             @HeaderParam(REQUEST_HEADER_NAME_FILTER) String filter,
                             @HeaderParam(REQUEST_HEADER_NAME_INTERCEPTOR) String interceptor,
                             @HeaderParam(REQUEST_HEADER_NAME_MBW) String mbw,
                             @HeaderParam(REQUEST_HEADER_MODIFICATION_SUPPORTED) boolean modificationSupported)
                throws IOException {
            assertEquals(REQUEST_HEADER_VALUE_CLIENT, client);
            assertEquals(REQUEST_HEADER_VALUE_FILTER, filter);
            if (modificationSupported) {
                assertEquals(REQUEST_HEADER_VALUE_INTERCEPTOR, interceptor);
                assertEquals(REQUEST_HEADER_VALUE_MBW, mbw);
            }
            assertEquals(QUESTION, new Scanner(GZIP ? new GZIPInputStream(questionStream) : questionStream).nextLine());
            return ANSWER;
        }
    }

    public static class MyWriterInterceptor implements WriterInterceptor {

        private final boolean addHeader;

        public MyWriterInterceptor(boolean addHeader) {
            this.addHeader = addHeader;
        }

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            if (addHeader) {
                context.getHeaders().add(REQUEST_HEADER_NAME_INTERCEPTOR, REQUEST_HEADER_VALUE_INTERCEPTOR);
            }
            if (GZIP) {
                context.setOutputStream(new GZIPOutputStream(context.getOutputStream()));
            }
            context.proceed();
        }
    }

    public static class MyClientRequestFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().add(REQUEST_HEADER_NAME_FILTER, REQUEST_HEADER_VALUE_FILTER);
        }
    }

    @Priority(Priorities.ENTITY_CODER)
    public static class MyMessageBodyWriter implements MessageBodyWriter<MyEntity> {

        private final boolean addHeader;

        public MyMessageBodyWriter(boolean addHeader) {
            this.addHeader = addHeader;
        }

        @Override
        public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        @Override
        public long getSize(MyEntity o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1; //ignored
        }

        @Override
        public void writeTo(MyEntity o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            if (addHeader) {
                httpHeaders.add(REQUEST_HEADER_NAME_MBW, REQUEST_HEADER_VALUE_MBW);
            }
            entityStream.write(o.getValue().getBytes());
        }
    }

    public static class MyEntity {

        private String value;

        public MyEntity() {
        }

        public MyEntity(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
