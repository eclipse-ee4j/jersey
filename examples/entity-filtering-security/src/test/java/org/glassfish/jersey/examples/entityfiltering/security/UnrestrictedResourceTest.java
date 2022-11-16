/*
 * Copyright (c) 2013, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.security;

import javax.ws.rs.core.Feature;

import org.glassfish.jersey.examples.entityfiltering.security.domain.RestrictedEntity;
import org.glassfish.jersey.examples.entityfiltering.security.domain.RestrictedSubEntity;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.filtering.SecurityEntityFilteringFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link org.glassfish.jersey.examples.entityfiltering.security.resource.UnrestrictedResource} unit tests.
 *
 * @author Michal Gajdos
 */
public class UnrestrictedResourceTest {

    public static Iterable<Class<? extends Feature>> providers() {
        return Arrays.asList(MoxyJsonFeature.class, JacksonFeature.class);
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        providers().forEach(feature -> {
            UnrestrictedResourceTemplateTest test = new UnrestrictedResourceTemplateTest(feature) {};
            tests.add(TestHelper.toTestContainer(test, feature.getSimpleName()));
        });
        return tests;
    }

    public static class UnrestrictedResourceTemplateTest extends JerseyTest {
        public UnrestrictedResourceTemplateTest(final Class<? extends Feature> filteringProvider) {
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
}
