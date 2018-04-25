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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.examples.extendedwadl.Item;

import org.codehaus.jettison.json.JSONArray;

/**
 * This resource is used to manage a single item.
 *
 * @author Martin Grotzke (martin.grotzke@freiheit.com)
 */
public class ItemResource {

    private final Item _item;

    public ItemResource(Item item) {
        _item = item;
    }

    /**
     * Typically returns the item if it exists. Please be aware that this method is extremely
     * expensive, so we can't guarantee that getting items is all the time possible.
     *
     * @response.representation.200.qname {http://www.example.com}item
     * @response.representation.200.mediaType application/xml
     * @response.representation.200.doc This is the representation returned by default
     *                                  (if we have an even number of millis since 1970...:)
     * @response.representation.200.example {@link org.glassfish.jersey.examples.extendedwadl.util.Examples#SAMPLE_ITEM}
     *
     * @response.representation.503.mediaType text/plain
     * @response.representation.503.example You'll get some explanation why this service is not available
     *
     * @return the requested item if this service is available, otherwise a 503.
     */
    @GET
    @Produces({"application/xml", "text/plain"})
    public Response getItem() {
        if (System.currentTimeMillis() % 2 == 0) {
            return Response.status(Status.SERVICE_UNAVAILABLE)
                    .entity("Sorry, but right now we can't process this request,"
                            + " try again an odd number of milliseconds later, please :)")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        return Response.ok(_item).build();
    }

    /**
     * Tries hard to return the item if it exists. If "Try-Hard" header is set to "true", the method is guaranteed to always
     * complete successfully if the item exists.
     *
     * @request.param {@name Try-Hard}
     *                  {@style header}
     *                  {@type {http://www.w3.org/2001/XMLSchema}string}
     *                  {@doc If set to "true", the call will always succeed provided the item exists.}
     *
     * @response.representation.200.qname {http://www.example.com}item
     * @response.representation.200.mediaType application/xml
     * @response.representation.200.doc This is the representation returned by default
     *                                  (if we have an even number of millis since 1970...:)
     * @response.representation.200.example {@link org.glassfish.jersey.examples.extendedwadl.util.Examples#SAMPLE_ITEM}
     *
     * @response.representation.503.mediaType text/plain
     * @response.representation.503.example You'll get some explanation why this service is not available.
     *
     * @return the requested item if it exists and the "Try-Hard" header is set to "true", otherwise a 503.
     */
    // Method added to reproduce OWLS-24243 issue.
    @GET
    @Produces({"application/xml", "text/plain"})
    @Path("try-hard")
    public Response getItem(@HeaderParam("Try-Hard") boolean tryHard) {
        if (!tryHard) {
            return Response.status(Status.SERVICE_UNAVAILABLE)
                    .entity("Sorry, but right now we can't process this request,"
                            + " try again and set the \"Try-Hard\" header to \"true\"")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
        return Response.ok(_item).build();
    }


    /**
     * Returns the item if existing.
     *
     * @response.representation.200.mediaType application/json
     * @response.representation.200.example ["myValue"]
     *
     * @return the requested item.
     */
    @GET
    @Produces({"application/json"})
    public JSONArray getItemAsJSON() {
        final JSONArray result = new JSONArray();
        result.put(_item.getValue());
        return result;
    }

    /**
     * Update the value property of the current item.
     *
     * @param value the new value to set
     */
    @Path("value/{value}")
    @PUT
    @Consumes({"application/xml"})
    public void updateItemValue(@PathParam("value") String value) {
        _item.setValue(value);
    }

}
