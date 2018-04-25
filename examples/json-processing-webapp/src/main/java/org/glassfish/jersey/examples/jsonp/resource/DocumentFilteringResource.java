/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonp.resource;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;

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
