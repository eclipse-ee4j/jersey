/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RedirectLargeFileTest {

    private static final int SERVER_PORT = 9997;
    private static final String SERVER_ADDR = String.format("http://localhost:%d/submit", SERVER_PORT);

    Client client() {
        final ClientConfig config = new ClientConfig();
        config.connectorProvider(new NettyConnectorProvider());
        config.register(MultiPartFeature.class);
        return ClientBuilder.newClient(config);
    }

    @BeforeAll
    static void startServer() throws Exception{
        RedirectFileUploadServerTest.start(SERVER_PORT);
    }

    @AfterAll
    static void stopServer() {
        RedirectFileUploadServerTest.stop();
    }

    @Test
    void sendFileTest() throws Exception {

        final String fileName = "bigFile.json";
        final String path = "target/" + fileName;

        final Path pathResource = Paths.get(path);
        try {
            final Path realFilePath = Files.createFile(pathResource.toAbsolutePath());

            generateJson(realFilePath.toString(), 1000000); // 33Mb real file size

            final byte[] content = Files.readAllBytes(realFilePath);

            final FormDataMultiPart mp = new FormDataMultiPart();
            mp.bodyPart(new FormDataBodyPart(FormDataContentDisposition.name(fileName).fileName(fileName).build(),
                    content,
                    MediaType.TEXT_PLAIN_TYPE));

            try (final Response response = client().target(SERVER_ADDR).request()
                    .post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA_TYPE))) {
                Assertions.assertEquals(200, response.getStatus());
            }
        } finally {
            Files.deleteIfExists(pathResource);
        }
    }

    private static void generateJson(final String filePath, int recordCount) throws Exception {

        try (final JsonGenerator generator = new JsonFactory().createGenerator(new FileWriter(filePath))) {
            generator.writeStartArray();

            for (int i = 0; i < recordCount; i++) {
                generator.writeStartObject();
                generator.writeNumberField("id", i);
                generator.writeStringField("name", "User" + i);
                // Add more fields as needed
                generator.writeEndObject();

                if (i % 10000 == 0) {
                    generator.flush();
                }
            }

            generator.writeEndArray();
        }
    }
}
