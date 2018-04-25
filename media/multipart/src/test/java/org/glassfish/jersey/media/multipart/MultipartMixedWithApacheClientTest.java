/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.multipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 */
public class MultipartMixedWithApacheClientTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(ProducesFormDataUsingMultiPart.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new ApacheConnectorProvider());
        config.register(MultiPartFeature.class);
    }

    @Path("resource")
    public static class ProducesFormDataUsingMultiPart {

        @POST
        @Consumes("multipart/mixed")
        public void post(MultiPart mp) throws IOException {
            byte[] in = read(mp.getBodyParts().get(0).getEntityAs(InputStream.class));
            assertEquals(50, in.length);

            in = read(mp.getBodyParts().get(1).getEntityAs(InputStream.class));
            assertEquals(900 * 1024, in.length);
        }

        private byte[] read(InputStream in) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read = -1;
            while ((read = in.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            return baos.toByteArray();
        }
    }

    // Test a response of type "multipart/form-data".  The example comes from
    // Section 6 of RFC 1867.
    @Test
    public void testProducesFormDataUsingMultiPart() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 900 * 1024; i++) {
            baos.write(65);
        }

        MultiPart multiPartInput = new MultiPart()
                .bodyPart(new ByteArrayInputStream("01234567890123456789012345678901234567890123456789".getBytes()),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .bodyPart(baos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        target().path("resource").request().post(Entity.entity(multiPartInput,
                MultiPartMediaTypes.createMixed()));
    }

    @Test
    public void testChunkedEncodingUsingMultiPart() {
        final Client client = client();
        client.property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 900 * 1024; i++) {
            baos.write(65);
        }

        MultiPart multiPartInput = new MultiPart()
                .bodyPart(new ByteArrayInputStream("01234567890123456789012345678901234567890123456789".getBytes()),
                        MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .bodyPart(baos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        client.target(getBaseUri()).path("resource").request()
                .post(Entity.entity(multiPartInput, MultiPartMediaTypes.createMixed()));
    }
}
