/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.linking.resources;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
            @Context javax.ws.rs.core.UriInfo info,
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
