/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestHelper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MultiPartTest {

    private static final Logger LOGGER = Logger.getLogger(RequestHeaderModificationsTest.class.getName());

    public static ConnectorProvider[] testData() {
        final ConnectorProvider[] providers = new ConnectorProvider[] {
                new HttpUrlConnectorProvider(),
                new NettyConnectorProvider(),
                new JdkConnectorProvider(),
                new JettyConnectorProvider(),
        };
        return providers;
    }


    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        for (ConnectorProvider provider : testData()) {
            HttpMultipartTest test = new HttpMultipartTest(provider) {};
            DynamicContainer container = TestHelper.toTestContainer(test,
                    String.format("MultiPartTest (%s)", provider.getClass().getSimpleName()));
            tests.add(container);
        }
        return tests;
    }

    public abstract static class HttpMultipartTest extends JerseyTest {
        private final ConnectorProvider connectorProvider;
        private static final String ENTITY = "hello";

        public HttpMultipartTest(ConnectorProvider connectorProvider) {
            this.connectorProvider = connectorProvider;
        }

        @Override
        protected Application configure() {
            set(TestProperties.RECORD_LOG_LEVEL, Level.WARNING.intValue());
            enable(TestProperties.LOG_TRAFFIC);
            return new ResourceConfig(MultipartResource.class)
                    .register(MultiPartFeature.class)
                    .register(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.HEADERS_ONLY));
        }

        @Override
        protected void configureClient(ClientConfig clientConfig) {
            clientConfig.connectorProvider(connectorProvider);
            clientConfig.register(MultiPartFeature.class);
        }

        @Path("/")
        public static class MultipartResource {
            @POST
            @Path("/upload")
            @Consumes(MediaType.MULTIPART_FORM_DATA)
            public String upload(@Context HttpHeaders headers, MultiPart multiPart) throws IOException {
                return ReaderWriter.readFromAsString(
                        ((BodyPartEntity) multiPart.getBodyParts().get(0).getEntity()).getInputStream(),
                        multiPart.getMediaType());
            }
        }

        @Test
        public void testMultipart() {
            MultiPart multipart = new MultiPart().bodyPart(new BodyPart().entity(ENTITY));
            multipart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);

            for (int i = 0; i != 5; i++) {
                try (Response r = target().register(MultiPartFeature.class)
                        .path("upload")
                        .request()
                        .post(Entity.entity(multipart, multipart.getMediaType()))) {
                    Assertions.assertEquals(Response.Status.OK.getStatusCode(), r.getStatus());
                    Assertions.assertEquals(ENTITY, r.readEntity(String.class));
                }
            }
        }

        @Test
        public void testNettyBufferedMultipart() {
//            setDebugLevel(Level.FINEST);
            ClientConfig config = new ClientConfig();

            config.connectorProvider(new NettyConnectorProvider());
            config.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.BUFFERED);
            config.register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
            config.register(new LoggingHandler(LogLevel.DEBUG));
            config.register(new LoggingInterceptor());
            config.property(ClientProperties.ASYNC_THREADPOOL_SIZE, 10);
            config.property("jersey.config.client.logging.verbosity", LoggingFeature.Verbosity.PAYLOAD_TEXT);
            config.property("jersey.config.client.logging.logger.level", Level.FINEST.toString());

            Client client = ClientBuilder.newClient(config);

            FormDataMultiPart formData = new FormDataMultiPart();
            FormDataBodyPart bodyPart1 = new FormDataBodyPart("hello1", "{\"first\":\"firstLine\",\"second\":\"secondLine\"}",
                    MediaType.APPLICATION_JSON_TYPE);
            formData.bodyPart(bodyPart1);
            formData.bodyPart(new FormDataBodyPart("hello2",
                    "{\"first\":\"firstLine\",\"second\":\"secondLine\",\"third\":\"thirdLine\"}",
                    MediaType.APPLICATION_JSON_TYPE));
            formData.bodyPart(new FormDataBodyPart("hello3",
                    "{\"first\":\"firstLine\",\"second\":\"secondLine\",\""
                            + "second\":\"secondLine\",\"second\":\"secondLine\",\"second\":\"secondLine\"}",
                    MediaType.APPLICATION_JSON_TYPE));
            formData.bodyPart(new FormDataBodyPart("plaintext", "hello"));

            Response response1 = client.target(target().getUri()).path("upload")
                    .request()
                    .post(Entity.entity(formData, formData.getMediaType()));

            MatcherAssert.assertThat(response1.getStatus(), Matchers.is(200));
            MatcherAssert.assertThat(response1.readEntity(String.class),
                    Matchers.stringContainsInOrder("first", "firstLine", "second", "secondLine"));
            response1.close();
            client.close();
        }

        public static void setDebugLevel(Level newLvl) {
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            rootLogger.setLevel(newLvl);
            for (Handler h : handlers) {
                h.setLevel(Level.ALL);
            }
            Logger nettyLogger = Logger.getLogger("io.netty");
            nettyLogger.setLevel(Level.FINEST);
        }

        @Provider
        public class LoggingInterceptor implements WriterInterceptor {

            @Override
            public void aroundWriteTo(WriterInterceptorContext context)
                    throws IOException, WebApplicationException {
                try {
                    MultivaluedMap<String, Object> headers = context.getHeaders();
                    headers.forEach((key, val) -> System.out.println(key + ":" + val));
                    context.proceed();
                } catch (Exception e) {
                    throw e;
                }
            }
        }
    }
}
