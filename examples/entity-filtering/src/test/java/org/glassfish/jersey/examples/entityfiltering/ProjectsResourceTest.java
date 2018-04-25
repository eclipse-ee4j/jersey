/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.examples.entityfiltering.domain.Project;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link org.glassfish.jersey.examples.entityfiltering.resource.UsersResource} unit tests.
 *
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class ProjectsResourceTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][]{{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    public ProjectsResourceTest(final Class<Feature> filteringProvider) {
        super(new ResourceConfig(EntityFilteringFeature.class)
                .packages("org.glassfish.jersey.examples.entityfiltering.resource")
                .register(filteringProvider));

        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Test
    public void testProjects() throws Exception {
        for (final Project project : target("projects").request().get(new GenericType<List<Project>>() {})) {
            testProject(project, false);
        }
    }

    @Test
    public void testProject() throws Exception {
        testProject(target("projects").path("1").request().get(Project.class), false);
    }

    @Test
    public void testDetailedProjects() throws Exception {
        for (final Project project : target("projects/detailed").request().get(new GenericType<List<Project>>() {})) {
            testProject(project, true);
        }
    }

    @Test
    public void testDetailedProject() throws Exception {
        testProject(target("projects/detailed").path("1").request().get(Project.class), true);
    }

    private void testProject(final Project project, final boolean isDetailed) {
        // Following properties should be in every returned project.
        assertThat(project.getId(), notNullValue());
        assertThat(project.getName(), notNullValue());
        assertThat(project.getDescription(), notNullValue());

        // Tasks and users should be only in "detailed" view.
        if (!isDetailed) {
            assertThat("Users present in non-detailed project view", project.getUsers(), nullValue());
            assertThat("Tasks present in non-detailed project view", project.getTasks(), nullValue());
        } else {
            assertThat("Users not present in detailed project view", project.getUsers(), notNullValue());
            assertThat("Tasks not present in detailed project view", project.getTasks(), notNullValue());
        }
    }
}
