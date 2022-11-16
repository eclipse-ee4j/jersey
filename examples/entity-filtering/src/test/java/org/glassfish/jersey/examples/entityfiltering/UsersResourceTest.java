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

import org.glassfish.jersey.examples.entityfiltering.domain.User;
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
 * {@link org.glassfish.jersey.examples.entityfiltering.resource.UsersResource} unit tests.
 *
 * @author Michal Gajdos
 */
public class UsersResourceTest {

    public static Iterable<Class<? extends Feature>> providers() {
        return Arrays.asList(MoxyJsonFeature.class, JacksonFeature.class);
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        providers().forEach(feature -> {
            UsersResourceTemplateTest test = new UsersResourceTemplateTest(feature) {};
            tests.add(TestHelper.toTestContainer(test, feature.getSimpleName()));
        });
        return tests;
    }

    public abstract static class UsersResourceTemplateTest extends JerseyTest {
        public UsersResourceTemplateTest(final Class<? extends Feature> filteringProvider) {
            super(new ResourceConfig(EntityFilteringFeature.class)
                    .packages("org.glassfish.jersey.examples.entityfiltering.resource")
                    .register(filteringProvider));

            enable(TestProperties.DUMP_ENTITY);
            enable(TestProperties.LOG_TRAFFIC);
        }

        @Test
        public void testUsers() throws Exception {
            for (final User user : target("users").request().get(new GenericType<List<User>>() {})) {
                testUser(user, false);
            }
        }

        @Test
        public void testUser() throws Exception {
            testUser(target("users").path("1").request().get(User.class), false);
        }

        @Test
        public void testDetailedUsers() throws Exception {
            for (final User user : target("users").queryParam("detailed", true).request().get(new GenericType<List<User>>() {})) {
                testUser(user, true);
            }
        }

        @Test
        public void testDetailedUser() throws Exception {
            testUser(target("users").path("1").queryParam("detailed", true).request().get(User.class), true);
        }

        private void testUser(final User user, final boolean isDetailed) {
            // Following properties should be in every returned user.
            assertThat(user.getId(), notNullValue());
            assertThat(user.getName(), notNullValue());
            assertThat(user.getEmail(), notNullValue());

            // Tasks and users should be in "detailed" view.
            if (!isDetailed) {
                assertThat(user.getProjects(), nullValue());
                assertThat(user.getTasks(), nullValue());
            } else {
                assertThat(user.getProjects(), notNullValue());
                assertThat(user.getTasks(), notNullValue());
            }
        }
    }
}
