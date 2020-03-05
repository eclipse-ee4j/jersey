/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.linking.resources;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.examples.linking.model.ItemsModel;
import org.glassfish.jersey.examples.linking.representation.ItemsRepresentation;

/**
 * Resource that provides access to the entire list of items
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
@Path("items")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class ItemsResource {

    private ItemsModel itemsModel;

    public ItemsResource() {
        itemsModel = ItemsModel.getInstance();
    }

    @GET
    public ItemsRepresentation query(
            @Context jakarta.ws.rs.core.UriInfo info,
            @QueryParam("offset") @DefaultValue("-1") int offset, @DefaultValue("-1") @QueryParam("limit") int limit) {

        if (offset == -1 || limit == -1) {
            offset = offset == -1 ? 0 : offset;
            limit = limit == -1 ? 10 : limit;

            throw new WebApplicationException(
                    Response.seeOther(info.getRequestUriBuilder().queryParam("offset", offset)
                            .queryParam("limit", limit).build())
                            .build()
            );
        }

        return new ItemsRepresentation(itemsModel, offset, limit);
    }

    @Path("{id}")
    public ItemResource get(@PathParam("id") String id) {
        return new ItemResource(itemsModel, id);
    }

}
