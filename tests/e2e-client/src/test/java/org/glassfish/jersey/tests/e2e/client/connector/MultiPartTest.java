/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiPartTest {

    private static final Logger LOGGER = Logger.getLogger(RequestHeaderModificationsTest.class.getName());

    public static List<ConnectorProvider> testData() {
        return Arrays.asList(
                new HttpUrlConnectorProvider(),
                new JettyConnectorProvider(),
                new NettyConnectorProvider(),
                new JdkConnectorProvider()
        );
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
    }
}
