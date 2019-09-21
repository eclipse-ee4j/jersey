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

import org.glassfish.jersey.examples.entityfiltering.domain.Task;
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
 * {@link org.glassfish.jersey.examples.entityfiltering.resource.TasksResource} unit tests.
 *
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class TaskResourceTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][]{{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    public TaskResourceTest(final Class<Feature> filteringProvider) {
        super(new ResourceConfig(EntityFilteringFeature.class)
                .packages("org.glassfish.jersey.examples.entityfiltering.resource")
                .register(filteringProvider));

        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Test
    public void testTasks() throws Exception {
        for (final Task task : target("tasks").request().get(new GenericType<List<Task>>() {})) {
            testTask(task, false);
        }
    }

    @Test
    public void testTask() throws Exception {
        testTask(target("tasks").path("1").request().get(Task.class), false);
    }

    @Test
    public void testDetailedTasks() throws Exception {
        for (final Task task : target("tasks").path("detailed").request().get(new GenericType<List<Task>>() {})) {
            testTask(task, true);
        }
    }

    @Test
    public void testDetailedTask() throws Exception {
        testTask(target("tasks").path("1").queryParam("detailed", true).request().get(Task.class), true);
    }

    private void testTask(final Task task, final boolean isDetailed) {
        // Following properties should be in every returned task.
        assertThat(task.getId(), notNullValue());
        assertThat(task.getName(), notNullValue());
        assertThat(task.getDescription(), notNullValue());

        // Tasks and tasks should be in "detailed" view.
        if (!isDetailed) {
            assertThat(task.getProject(), nullValue());
            assertThat(task.getUser(), nullValue());
        } else {
            assertThat(task.getProject(), notNullValue());
            assertThat(task.getUser(), notNullValue());
        }
    }
}
