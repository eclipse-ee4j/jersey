/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonp;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;

import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Michal Gajdos
 */
public class JsonProcessingResourceTest extends JerseyTest {

    private static final List<JsonObject> documents = new ArrayList<>();

    static {
        documents.add(Json.createObjectBuilder()
                        .add("name", "Jersey")
                        .add("site", "http://jersey.github.io")
                        .build()
        );
        documents.add(Json.createObjectBuilder()
                        .add("age", 33)
                        .add("phone", "158158158")
                        .add("name", "Foo")
                        .build()
        );
        documents.add(Json.createObjectBuilder()
                        .add("name", "JSON-P")
                        .add("site", "https://javaee.github.io/jsonp/")
                        .build()
        );
    }

    @Override
    protected Application configure() {
        return new MyApplication();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("json-processing-webapp").build();
    }

    @Test
    public void testStoreGetRemoveDocument() throws Exception {
        final JsonObject document = documents.get(0);

        // Store.
        final Response response = target("document").request(MediaType.APPLICATION_JSON).post(Entity.json(document));
        assertEquals(200, response.getStatus());

        final List<JsonNumber> ids = response.readEntity(JsonArray.class).getValuesAs(JsonNumber.class);
        assertEquals(1, ids.size());

        // Get.
        final String id = ids.get(0).toString();
        final WebTarget documentTarget = target("document").path(id);
        final JsonObject storedDocument = documentTarget.request(MediaType.APPLICATION_JSON)
                .get(JsonObject.class);
        assertEquals(document, storedDocument);

        // Remove.
        final JsonObject removedDocument = documentTarget.request(MediaType.APPLICATION_JSON).delete(JsonObject.class);
        assertEquals(document, removedDocument);

        // Get.
        final Response errorResponse = documentTarget.request(MediaType.APPLICATION_JSON).get();
        assertEquals(204, errorResponse.getStatus());
    }

    private JsonArray getDocumentJsonArray() {
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (final JsonObject document : documents) {
            arrayBuilder.add(document);
        }
        return arrayBuilder.build();
    }

    @Test
    public void testStoreDocuments() throws Exception {
        final Response response = target("document/multiple")
                .request(MediaType.APPLICATION_JSON).post(Entity.json(getDocumentJsonArray()));

        assertEquals(200, response.getStatus());

        final List<JsonNumber> ids = response.readEntity(JsonArray.class).getValuesAs(JsonNumber.class);
        assertEquals(documents.size(), ids.size());

        // Remove All.
        target("document").request().delete();
    }

    @Test
    public void testFilterDocuments() throws Exception {
        // Store documents.
        target("document/multiple").request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(getDocumentJsonArray()));

        // Filter.
        JsonArray filter = Json.createArrayBuilder().add("site").build();
        JsonArray filtered = target("document/filter").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(filter), JsonArray.class);

        checkFilteredDocuments(filtered, 2, "site");

        filter = Json.createArrayBuilder().add("site").add("age").build();
        filtered = target("document/filter").request(MediaType.APPLICATION_JSON)
                .post(Entity.json(filter), JsonArray.class);

        checkFilteredDocuments(filtered, 3, "site", "age");

        // Remove All.
        target("document").request().delete();
    }

    private void checkFilteredDocuments(final JsonArray filtered, final int size, final String... properties) {
        assertEquals(size, filtered.size());

        Set<String> strings = Arrays.stream(properties).collect(Collectors.toSet());
        for (final JsonObject document : filtered.getValuesAs(JsonObject.class)) {
            for (final String property : document.keySet()) {
                assertTrue(strings.contains(property));
            }
        }
    }
}
