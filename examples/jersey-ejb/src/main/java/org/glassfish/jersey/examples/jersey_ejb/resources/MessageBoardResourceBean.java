/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jersey_ejb.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.glassfish.jersey.examples.jersey_ejb.entities.Message;
import org.glassfish.jersey.examples.jersey_ejb.exceptions.CustomNotFoundException;

/**
 * A stateless EJB bean to handle REST requests to the messages resource.
 * Messages are stored in the injected EJB singleton instance.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Stateless
public class MessageBoardResourceBean {

    @Context
    private UriInfo ui;
    @EJB
    MessageHolderSingletonBean singleton;

    /**
     * Returns a list of all messages stored in the internal message holder.
     */
    @GET
    public List<Message> getMessages() {
        return singleton.getMessages();
    }

    @POST
    public Response addMessage(String msg) throws URISyntaxException {
        Message m = singleton.addMessage(msg);

        URI msgURI = ui.getRequestUriBuilder().path(Integer.toString(m.getUniqueId())).build();

        return Response.created(msgURI).build();
    }

    @Path("{msgNum}")
    @GET
    public Message getMessage(@PathParam("msgNum") int msgNum) {
        Message m = singleton.getMessage(msgNum);

        if (m == null) {
            // This exception will be passed through to the JAX-RS runtime
            // No other runtime exception will behave this way unless the
            // exception is annotated with javax.ejb.ApplicationException
            throw new NotFoundException();
        }

        return m;

    }

    @Path("{msgNum}")
    @DELETE
    public void deleteMessage(@PathParam("msgNum") int msgNum) throws CustomNotFoundException {
        boolean deleted = singleton.deleteMessage(msgNum);

        if (!deleted) {
            // This exception will be mapped to a 404 response
            throw new CustomNotFoundException();
        }
    }
}





