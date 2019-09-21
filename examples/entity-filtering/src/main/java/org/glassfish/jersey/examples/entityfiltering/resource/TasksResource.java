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
import javax.ws.rs.core.Response;

import org.glassfish.jersey.examples.entityfiltering.domain.EntityStore;
import org.glassfish.jersey.examples.entityfiltering.domain.Task;
import org.glassfish.jersey.examples.entityfiltering.filtering.TaskDetailedView;

/**
 * Resource class for {@link Task tasks}.
 *
 * @author Michal Gajdos
 */
@Path("tasks")
@Produces("application/json")
public class TasksResource {

    @GET
    public List<Task> getTasks() {
        return getDetailedTasks();
    }

    @GET
    @Path("detailed")
    @TaskDetailedView
    public List<Task> getDetailedTasks() {
        return EntityStore.getTasks();
    }

    @GET
    @Path("{id}")
    public Response getTask(@PathParam("id") final Long id, @QueryParam("detailed") final boolean isDetailed) {
        return Response
                .ok()
                .entity(EntityStore.getTask(id),
                        isDetailed ? new Annotation[] {TaskDetailedView.Factory.get()} : new Annotation[0])
                .build();
    }
}
