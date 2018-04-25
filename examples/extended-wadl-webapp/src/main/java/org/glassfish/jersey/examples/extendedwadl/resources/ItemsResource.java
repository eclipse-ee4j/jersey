/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.extendedwadl.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.inject.Singleton;

import org.glassfish.jersey.examples.extendedwadl.Item;

/**
 * This is the root resource for managing items.
 *
 * @author Martin Grotzke (martin.grotzke@freiheit.com)
 */
@Singleton
@Path("items")
public class ItemsResource {

    @Context
    UriInfo _uriInfo;
    private final AtomicInteger _sequence;
    private final Map<Integer, Item> _repository;

    public ItemsResource() {
        _sequence = new AtomicInteger();
        _repository = new HashMap<>();
    }

    /**
     * Get an item resource for an item from the list of managed items based on the assigned id extracted from the path parameter.
     *
     * @param id The ID of the item to retrieve.
     * @return respective items resource.
     *
     */
    @Path("{id}")
    public ItemResource getItem(@PathParam("id") final Integer id) {
        final Item item = _repository.get(id);
        if (item == null) {
            throw new NotFoundException(Response.status(Response.Status.NOT_FOUND).entity("Item with id " + id + " does not "
                    + "exist!").build());
        }

        return new ItemResource(item);
    }

    /**
     * Add a new item to the list of managed items. The item will get assigned an id,
     * the resource where the item is available will be returned in the location header.
     *
     * @param item The item to create.
     *
     * @request.representation.qname {http://www.example.com}item
     * @request.representation.mediaType application/xml
     * @request.representation.example {@link org.glassfish.jersey.examples.extendedwadl.util.Examples#SAMPLE_ITEM}
     *
     * @response.param {@name Location}
     *                  {@style header}
     *                  {@type {http://www.w3.org/2001/XMLSchema}anyURI}
     *                  {@doc The URI where the created item is accessable.}
     *
     * @return The response with the status code and the location header.
     *
     */
    @POST
    @Consumes({"application/xml"})
    public Response createItem(Item item) {
        final Integer id = _sequence.incrementAndGet();
        _repository.put(id, item);
        return Response.created(
                _uriInfo.getAbsolutePathBuilder().clone().path(id.toString()).build())
                .build();
    }

}
