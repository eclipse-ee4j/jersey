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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link org.glassfish.jersey.examples.entityfiltering.security.resource.UnrestrictedResource} unit tests.
 *
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class UnrestrictedResourceTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][]{{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    public UnrestrictedResourceTest(final Class<Feature> filteringProvider) {
        super(new ResourceConfig(SecurityEntityFilteringFeature.class)
                .packages("org.glassfish.jersey.examples.entityfiltering.security")
                .register(filteringProvider));

        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Test
    public void testRestrictedEntity() throws Exception {
        final RestrictedEntity entity = target("unrestricted-resource").request().get(RestrictedEntity.class);
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
}
