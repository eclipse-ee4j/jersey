/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

/**
 * Very basic resource example showcases CRUD functionality
 * implemented via HTTP POST, GET, PUT and DELETE methods.
 * A simple clipboard is simulated which is capable of handling
 * text data only.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
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
