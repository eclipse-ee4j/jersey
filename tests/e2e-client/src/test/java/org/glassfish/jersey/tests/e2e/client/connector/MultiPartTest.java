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
import org.glassfish.jersey.internal.util.JdkVersion;
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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MultiPartTest {

    private static final Logger LOGGER = Logger.getLogger(RequestHeaderModificationsTest.class.getName());

    public static ConnectorProvider[] testData() {
        int size = JdkVersion.getJdkVersion().getMajor() < 11 ? 3 : 4;
        final ConnectorProvider[] providers = new ConnectorProvider[size];
        providers[0] = new HttpUrlConnectorProvider();
        providers[1] = new NettyConnectorProvider();
        providers[2] = new JdkConnectorProvider();
        if (size == 4) {
            providers[3] = new JettyConnectorProvider();
        }
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
    }
}
