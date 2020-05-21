/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonp.resource;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;

import org.glassfish.jersey.examples.jsonp.service.DocumentStorage;

/**
 * Resource filtering stored documents based on the presence of given attributes.
 *
 * @author Michal Gajdos
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentFilteringResource {

    @POST
    public JsonArray filter(final JsonArray properties) {
        final JsonArrayBuilder documents = Json.createArrayBuilder();
        final List<JsonString> propertyList = properties.getValuesAs(JsonString.class);

        for (final JsonObject jsonObject : DocumentStorage.getAll()) {
            final JsonObjectBuilder documentBuilder = Json.createObjectBuilder();

            for (final JsonString property : propertyList) {
                final String key = property.getString();

                if (jsonObject.containsKey(key)) {
                    documentBuilder.add(key, jsonObject.get(key));
                }
            }

            final JsonObject document = documentBuilder.build();
            if (!document.isEmpty()) {
                documents.add(document);
            }
        }

        return documents.build();
    }

}
