/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.performance.benchmark.entity.json;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Resource class for {@link Project projects}. Provides methods to retrieve projects in "default" view ({@link #getProjects()}
 * and in "detailed" view ({@link #getDetailedProjects()}.
 *
 * @author Michal Gajdos
 */
@Path("projects")
@Produces("application/json")
public class ProjectsResource {

    private static final List<Project> projects;

    static {
        final Project project = new Project(1L, "foo", "bar");
        final User user = new User(1L, "foo", "foo@bar.baz");
        final Task task = new Task(1L, "foo", "bar");

        project.setUsers(Arrays.asList(user));
        project.setTasks(Arrays.asList(task, task));

        projects = Arrays.asList(project, project);
    }

    @GET
    @Path("basic")
    public List<Project> getProjects() {
        return getDetailedProjects();
    }

    @GET
    @Path("detailed")
    @ProjectDetailedView
    public List<Project> getDetailedProjects() {
        return projects;
    }
}
