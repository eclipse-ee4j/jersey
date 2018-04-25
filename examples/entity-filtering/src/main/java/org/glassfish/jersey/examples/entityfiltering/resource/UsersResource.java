/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.resource;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.examples.entityfiltering.domain.EntityStore;
import org.glassfish.jersey.examples.entityfiltering.domain.User;
import org.glassfish.jersey.examples.entityfiltering.filtering.UserDetailedView;

/**
 * Resource class for {@link User users}. Provides combined methods to retrieve users in "default" view and "detailed" view.
 * Detailed view can be triggered by appending {@code detailed} query param to the URI.
 * <p/>
 * To see the resource methods expanded refer to the {@link ProjectsResource}.
 *
 * @author Michal Gajdos
 * @see ProjectsResource
 */
@Path("users")
@Produces("application/json")
public class UsersResource {

    @GET
    public Response getUsers(@QueryParam("detailed") final boolean isDetailed) {
        return Response
                .ok()
                .entity(new GenericEntity<List<User>>(EntityStore.getUsers()) {},
                        isDetailed ? new Annotation[]{UserDetailedView.Factory.get()} : new Annotation[0])
                .build();
    }

    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") final Long id, @QueryParam("detailed") final boolean isDetailed) {
        return Response
                .ok()
                .entity(EntityStore.getUser(id),
                        isDetailed ? new Annotation[]{UserDetailedView.Factory.get()} : new Annotation[0])
                .build();
    }
}
