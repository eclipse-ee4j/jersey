/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link org.glassfish.jersey.examples.entityfiltering.resource.TasksResource} unit tests.
 *
 * @author Michal Gajdos
 */
public class TaskResourceTest {

    public static Iterable<Class<? extends Feature>> providers() {
        return Arrays.asList(MoxyJsonFeature.class, JacksonFeature.class);
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        providers().forEach(feature -> {
            TaskResourceTemplateTest test = new TaskResourceTemplateTest(feature) {};
            tests.add(TestHelper.toTestContainer(test, feature.getSimpleName()));
        });
        return tests;
    }

    public abstract static class TaskResourceTemplateTest extends JerseyTest {
        public TaskResourceTemplateTest(final Class<? extends Feature> filteringProvider) {
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
}
