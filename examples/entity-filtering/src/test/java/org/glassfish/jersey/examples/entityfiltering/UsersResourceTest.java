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

import org.glassfish.jersey.examples.entityfiltering.domain.User;
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
import static org.junit.Assert.assertThat;

/**
 * {@link org.glassfish.jersey.examples.entityfiltering.resource.UsersResource} unit tests.
 *
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class UsersResourceTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][]{{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    public UsersResourceTest(final Class<Feature> filteringProvider) {
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
