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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.glassfish.jersey.examples.entityfiltering.domain.EntityStore;
import org.glassfish.jersey.examples.entityfiltering.domain.Project;
import org.glassfish.jersey.examples.entityfiltering.filtering.ProjectDetailedView;

/**
 * Resource class for {@link Project projects}. Provides methods to retrieve projects in "default" view ({@link #getProjects()}
 * and {@link #getProject(Long)}) and in "detailed" view ({@link #getDetailedProjects()} and {@link #getDetailedProject(Long)}).
 * <p/>
 * To reduce the number of methods while keeping the functionality of resource unchanged (support for both "default" and
 * "detailed" view) see {@link UsersResource}.
 *
 * @author Michal Gajdos
 * @see UsersResource
 */
@Path("projects")
@Produces("application/json")
public class ProjectsResource {

    @GET
    @Path("{id}")
    public Project getProject(@PathParam("id") final Long id) {
        return getDetailedProject(id);
    }

    @GET
    public List<Project> getProjects() {
        return getDetailedProjects();
    }

    @GET
    @Path("detailed/{id}")
    @ProjectDetailedView
    public Project getDetailedProject(@PathParam("id") final Long id) {
        return EntityStore.getProject(id);
    }

    @GET
    @Path("detailed")
    @ProjectDetailedView
    public List<Project> getDetailedProjects() {
        return EntityStore.getProjects();
    }
}
