/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httppatch;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import javax.json.Json;
import javax.json.JsonArray;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * HTTP PATCH Example unit tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class HttpPatchTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        // Uncomment to enable message exchange logging
        // enable(TestProperties.DUMP_ENTITY);
        // enable(TestProperties.LOG_TRAFFIC);
        return App.create();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(App.createMoxyJsonResolver())
                .connectorProvider(new GrizzlyConnectorProvider());

    }

    /**
     * This test verifies that the patching of the resource state works.
     * <p>
     * The patch is created using the new standard JSON Processing API for Java and
     * is then sent to the server. {@code PATCH} response as well as the new resource
     * state obtained via subsequent {@code GET} method is verified against the expected
     * state.
     * </p>
     */
    @Test
    public void testPatch() {
        final WebTarget target = target(App.ROOT_PATH);

        // initial precondition check
        final State expected = new State();
        assertEquals(expected, target.request("application/json").get(State.class));

        // apply first patch
        expected.setMessage("patchedMessage");
        expected.setTitle("patchedTitle");
        expected.getList().add("one");
        expected.getList().add("two");

        JsonArray patch_1 = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("op", "replace")
                        .add("path", "/message")
                        .add("value", expected.getMessage())
                        .build())
                .add(Json.createObjectBuilder()
                        .add("op", "replace")
                        .add("path", "/title")
                        .add("value", expected.getTitle())
                        .build())
                .add(Json.createObjectBuilder()
                        .add("op", "replace")
                        .add("path", "/list")
                        .add("value", Json.createArrayBuilder()
                                .add(expected.getList().get(0))
                                .add(expected.getList().get(1))
                                .build())
                        .build())
                .build();

        assertEquals(expected, target.request()
                                     .method("PATCH",
                                             Entity.entity(patch_1, MediaType.APPLICATION_JSON_PATCH_JSON), State.class));
        assertEquals(expected, target.request("application/json").get(State.class));

        // apply second patch
        expected.getList().add("three");

        JsonArray patch_2 = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("op", "add")
                        .add("path", "/list/-")
                        .add("value", expected.getList().get(2))
                        .build())
                .build();
        assertEquals(expected, target.request()
                                     .method("PATCH",
                                             Entity.entity(patch_2, MediaType.APPLICATION_JSON_PATCH_JSON), State.class));
        assertEquals(expected, target.request("application/json").get(State.class));
    }
}
