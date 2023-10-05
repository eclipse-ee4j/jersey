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

package org.glassfish.jersey.integration.jackson14;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class Jackson14DependencyTest {

    @Test
    void testJackson15Feature() {
        try (Response response = ClientBuilder.newClient()
                .register(JacksonFeature.withExceptionMappers().maxStringLength(3))
                .register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        requestContext.abortWith(Response.ok(new TextNode("12345"))
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE)
                                .build());
                    }
                })
                .target("http://localhost:8080")
                .request().get()) {
            Assertions.assertEquals(200, response.getStatus());
            JsonNode node = response.readEntity(JsonNode.class);
            Assertions.assertEquals("12345", node.asText()); // Jackson 15 throws ProcessingException
        }
    }
}
