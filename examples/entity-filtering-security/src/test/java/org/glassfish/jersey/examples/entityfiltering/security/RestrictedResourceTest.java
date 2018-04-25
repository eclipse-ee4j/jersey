/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.security;

import java.util.Arrays;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.examples.entityfiltering.security.domain.RestrictedEntity;
import org.glassfish.jersey.examples.entityfiltering.security.domain.RestrictedSubEntity;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.filtering.SecurityEntityFilteringFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link org.glassfish.jersey.examples.entityfiltering.security.resource.RestrictedResource} unit tests.
 *
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class RestrictedResourceTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][]{{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    public RestrictedResourceTest(final Class<Feature> filteringProvider) {
        super(new ResourceConfig(SecurityEntityFilteringFeature.class)
                .packages("org.glassfish.jersey.examples.entityfiltering.security")
                .register(filteringProvider));

        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Test
    public void testDenyAll() throws Exception {
        assertThat(target("restricted-resource").path("denyAll").request().get().getStatus(),
                equalTo(Response.Status.FORBIDDEN.getStatusCode()));
    }

    @Test
    public void testPermitAll() throws Exception {
        final RestrictedEntity entity = target("restricted-resource").path("permitAll").request().get(RestrictedEntity.class);
        final RestrictedSubEntity mixedField = entity.getMixedField();

        // Not null values.
        assertThat(entity.getSimpleField(), notNullValue());
        assertThat(entity.getPermitAll(), notNullValue());

        // Null values.
        assertThat(entity.getDenyAll(), nullValue());
        assertThat(mixedField, nullValue());
    }

    @Test
    public void testRolesAllowed() throws Exception {
        final RestrictedEntity entity = target("restricted-resource").path("rolesAllowed").request().get(RestrictedEntity.class);
        final RestrictedSubEntity mixedField = entity.getMixedField();

        // Not null values.
        assertThat(entity.getSimpleField(), notNullValue());
        assertThat(entity.getPermitAll(), notNullValue());
        assertThat(mixedField, notNullValue());
        assertThat(mixedField.getManagerField(), notNullValue());

        // Null values.
        assertThat(entity.getDenyAll(), nullValue());
        assertThat(mixedField.getUserField(), nullValue());
    }

    @Test
    public void testRuntimeRolesAllowedUser() throws Exception {
        final RestrictedEntity entity = target("restricted-resource")
                .path("runtimeRolesAllowed")
                .queryParam("roles", "user")
                .request().get(RestrictedEntity.class);
        final RestrictedSubEntity mixedField = entity.getMixedField();

        // Not null values.
        assertThat(entity.getSimpleField(), notNullValue());
        assertThat(entity.getPermitAll(), notNullValue());
        assertThat(mixedField, notNullValue());
        assertThat(mixedField.getUserField(), notNullValue());

        // Null values.
        assertThat(entity.getDenyAll(), nullValue());
        assertThat(mixedField.getManagerField(), nullValue());
    }

    @Test
    public void testRuntimeRolesAllowedManagerUser() throws Exception {
        final RestrictedEntity entity = target("restricted-resource")
                .path("runtimeRolesAllowed")
                .queryParam("roles", "user,manager")
                .request().get(RestrictedEntity.class);
        final RestrictedSubEntity mixedField = entity.getMixedField();

        // Not null values.
        assertThat(entity.getSimpleField(), notNullValue());
        assertThat(entity.getPermitAll(), notNullValue());
        assertThat(mixedField, notNullValue());
        assertThat(mixedField.getUserField(), notNullValue());
        assertThat(mixedField.getManagerField(), notNullValue());

        // Null values.
        assertThat(entity.getDenyAll(), nullValue());
    }

    @Test
    public void testRuntimeRolesAllowedInvalid() throws Exception {
        final RestrictedEntity entity = target("restricted-resource")
                .path("runtimeRolesAllowed")
                .queryParam("roles", "invalid")
                .request().get(RestrictedEntity.class);
        final RestrictedSubEntity mixedField = entity.getMixedField();

        // Not null values.
        assertThat(entity.getSimpleField(), notNullValue());
        assertThat(entity.getPermitAll(), notNullValue());

        // Null values.
        assertThat(entity.getDenyAll(), nullValue());
        assertThat(mixedField, nullValue());
    }
}
