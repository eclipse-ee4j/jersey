/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.clipboard;

import java.util.LinkedList;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Variant;

/**
 * Very basic resource example showcases CRUD functionality
 * implemented via HTTP POST, GET, PUT and DELETE methods.
 * A simple clipboard is simulated which is capable of handling
 * text data only.
 *
 * @author Marek Potociar
 * @author Jakub Podlesak
 */
@Path("clipboard")
public class ClipboardResource {

    static final List<Variant> supportedVariants =
            Variant.mediaTypes(
                    MediaType.APPLICATION_JSON_TYPE,
                    MediaType.TEXT_PLAIN_TYPE).add().build();

    private static final List<String> history = new LinkedList<String>();

    private static ClipboardData content = new ClipboardData("");

    @Context
    Request request;

    @GET
    public Response content() {

        if (content.isEmpty()) {
            return Response.noContent().build();
        }

        final Variant variant = request.selectVariant(supportedVariants);

        if (variant == null) {
            return Response.notAcceptable(supportedVariants).build();
        } else {
            return Response.ok(content, variant.getMediaType()).build();
        }
    }

    @PUT
    @Consumes({"text/plain", "application/json"})
    public void setContent(ClipboardData newContent) {
        saveHistory();
        updateContent(newContent);
    }

    private static void updateContent(ClipboardData newContent) {
        content = newContent;
    }

    @POST
    @Consumes({"text/plain", "application/json"})
    @Produces({"text/plain", "application/json"})
    public ClipboardData append(ClipboardData appendix) {
        saveHistory();
        return content.append(appendix);
    }

    @DELETE
    public void clear() {
        saveHistory();
        content.clear();
    }

    @GET
    @Path("history")
    @Produces({"text/plain", "application/json"})
    public List<String> getHistory() {
        return history;
    }

    @DELETE
    @Path("history")
    public void clearHistory() {
        history.clear();
    }

    private void saveHistory() {
        String currentContent = content.toString();
        if (!currentContent.isEmpty()) {
            history.add(currentContent);
        }
    }
}
