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

package org.glassfish.jersey.tests.e2e.entity.filtering.json;

import java.util.Arrays;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Feature;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.entity.filtering.PrimaryDetailedView;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.ComplexEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.ComplexSubEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.ComplexSubSubEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class JsonEntityFilteringScopesTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][]{{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    public JsonEntityFilteringScopesTest(final Class<Feature> filteringProvider) {
        super(new ResourceConfig(Resource.class, EntityFilteringFeature.class).register(filteringProvider));

        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    public static class Resource {

        @GET
        @PrimaryDetailedView
        public ComplexEntity get() {
            return ComplexEntity.INSTANCE;
        }
    }

    /**
     * Primary -> Default -> Primary.
     */
    @Test
    public void testEntityFilteringScopes() throws Exception {
        final ComplexEntity entity = target().request().get(ComplexEntity.class);

        // ComplexEntity
        assertThat(entity.accessorTransient, is("propertyproperty"));
        assertThat(entity.getProperty(), is("property"));

        // ComplexSubEntity
        final ComplexSubEntity subEntity = entity.field;
        assertThat(subEntity, notNullValue());
        assertThat(subEntity.accessorTransient, is("fieldfield"));
        assertThat(subEntity.field, is("field"));

        // ComplexSubSubEntity
        final ComplexSubSubEntity subSubEntity = entity.field.getProperty();
        assertThat(subSubEntity.accessorTransient, is("fieldfield"));
        assertThat(subSubEntity.getProperty(), is("property"));
        assertThat(subSubEntity.field, nullValue());
    }
}
