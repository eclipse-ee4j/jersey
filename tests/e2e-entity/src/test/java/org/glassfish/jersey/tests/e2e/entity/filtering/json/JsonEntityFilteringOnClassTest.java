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

import java.lang.annotation.Annotation;
import java.util.Arrays;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.entity.filtering.DefaultFilteringScope;
import org.glassfish.jersey.tests.e2e.entity.filtering.PrimaryDetailedView;
import org.glassfish.jersey.tests.e2e.entity.filtering.SecondaryDetailedView;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.DefaultFilteringSubEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.FilteredClassEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.ManyFilteringsOnClassEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.ManyFilteringsSubEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.OneFilteringOnClassEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.OneFilteringSubEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Use-cases with entity-filtering annotations on class, JSON output.
 *
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class JsonEntityFilteringOnClassTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][] {{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    public JsonEntityFilteringOnClassTest(final Class<Feature> filteringProvider) {
        super(new ResourceConfig(Resource.class, EntityFilteringFeature.class).register(filteringProvider));

        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    public static class Resource {

        @GET
        @Path("OneFilteringEntity")
        @PrimaryDetailedView
        public OneFilteringOnClassEntity getOneFilteringEntity() {
            return OneFilteringOnClassEntity.INSTANCE;
        }

        @GET
        @Path("OneFilteringEntityDefaultView")
        public OneFilteringOnClassEntity getOneFilteringEntityDefaultView() {
            return OneFilteringOnClassEntity.INSTANCE;
        }

        @POST
        @Path("OneFilteringEntity")
        public String postOneFilteringEntity(final String value) {
            return value;
        }

        @GET
        @Path("OneFilteringEntityDefaultViewResponse")
        public Response getOneFilteringEntityDefaultViewResponse() {
            return Response.ok().entity(OneFilteringOnClassEntity.INSTANCE, new Annotation[] {new DefaultFilteringScope()})
                    .build();
        }

        @GET
        @Path("ManyFilteringsEntityPrimaryView")
        @PrimaryDetailedView
        public ManyFilteringsOnClassEntity getManyFilteringsEntityPrimaryView() {
            return ManyFilteringsOnClassEntity.INSTANCE;
        }

        @GET
        @Path("ManyFilteringsEntitySecondaryView")
        @SecondaryDetailedView
        public ManyFilteringsOnClassEntity getManyFilteringsEntitySecondaryView() {
            return ManyFilteringsOnClassEntity.INSTANCE;
        }

        @GET
        @Path("ManyFilteringsEntityDefaultView")
        public ManyFilteringsOnClassEntity getManyFilteringsEntityDefaultView() {
            return ManyFilteringsOnClassEntity.INSTANCE;
        }

        @GET
        @Path("ManyFilteringsEntityManyViews")
        @PrimaryDetailedView
        @SecondaryDetailedView
        public ManyFilteringsOnClassEntity getManyFilteringsEntityManyViews() {
            return ManyFilteringsOnClassEntity.INSTANCE;
        }
    }

    @Test
    public void testOneEntityFilteringOnClass() throws Exception {
        final OneFilteringOnClassEntity entity = target("OneFilteringEntity").request().get(OneFilteringOnClassEntity.class);

        // OneFilteringOnClassEntity
        assertThat(entity.field, is(10));
        assertThat(entity.accessorTransient, is("propertyproperty"));
        assertThat(entity.getProperty(), is("property"));

        // FilteredClassEntity
        final FilteredClassEntity filtered = entity.getFiltered();
        assertThat(filtered, notNullValue());
        assertThat(filtered.field, is(0));
        assertThat(filtered.getProperty(), nullValue());

        // DefaultFilteringSubEntity
        assertThat(entity.getDefaultEntities(), notNullValue());
        assertThat(entity.getDefaultEntities().size(), is(1));
        final DefaultFilteringSubEntity defaultFilteringSubEntity = entity.getDefaultEntities().get(0);
        assertThat(defaultFilteringSubEntity.field, is(true));
        assertThat(defaultFilteringSubEntity.getProperty(), is(20L));

        // OneFilteringSubEntity
        assertThat(entity.getSubEntities(), notNullValue());
        assertThat(entity.getSubEntities().size(), is(1));
        final OneFilteringSubEntity oneFilteringSubEntity = entity.getSubEntities().get(0);
        assertThat(oneFilteringSubEntity.field1, is(20));
        assertThat(oneFilteringSubEntity.field2, is(30));
        assertThat(oneFilteringSubEntity.getProperty1(), is("property1"));
        assertThat(oneFilteringSubEntity.getProperty2(), is("property2"));
    }

    @Test
    public void testOneEntityFilteringOnClassDefaultViewResponse() throws Exception {
        final OneFilteringOnClassEntity entity = target("OneFilteringEntityDefaultViewResponse").request()
                .get(OneFilteringOnClassEntity.class);

        // OneFilteringOnClassEntity
        assertThat(entity.field, is(0));
        assertThat(entity.accessorTransient, nullValue());
        assertThat(entity.getProperty(), nullValue());

        // FilteredClassEntity
        final FilteredClassEntity filtered = entity.getFiltered();
        assertThat(filtered, nullValue());

        // DefaultFilteringSubEntity
        assertThat(entity.getDefaultEntities(), nullValue());

        // OneFilteringSubEntity
        assertThat(entity.getSubEntities(), nullValue());
    }

    @Test
    public void testOneEntityFilteringOnClassDefaultView() throws Exception {
        final OneFilteringOnClassEntity entity = target("OneFilteringEntityDefaultView").request()
                .get(OneFilteringOnClassEntity.class);

        // OneFilteringOnClassEntity
        assertThat(entity.field, is(0));
        assertThat(entity.accessorTransient, nullValue());
        assertThat(entity.getProperty(), nullValue());

        // FilteredClassEntity
        final FilteredClassEntity filtered = entity.getFiltered();
        assertThat(filtered, nullValue());

        // DefaultFilteringSubEntity
        assertThat(entity.getDefaultEntities(), nullValue());

        // OneFilteringSubEntity
        assertThat(entity.getSubEntities(), nullValue());
    }

    @Test
    public void testMultipleViewsOnClass() throws Exception {
        testOneEntityFilteringOnClass();
        testOneEntityFilteringOnClassDefaultView();
    }

    @Test
    public void testManyFilteringsEntityPrimaryView() throws Exception {
        final ManyFilteringsOnClassEntity entity = target("ManyFilteringsEntityPrimaryView").request()
                .get(ManyFilteringsOnClassEntity.class);

        // ManyFilteringsOnClassEntity
        assertThat(entity.field, is(50));
        assertThat(entity.accessorTransient, is("propertyproperty"));
        assertThat(entity.getProperty(), is("property"));

        // FilteredClassEntity
        final FilteredClassEntity filtered = entity.filtered;
        assertThat(filtered, notNullValue());
        assertThat(filtered.field, is(0));
        assertThat(filtered.getProperty(), nullValue());

        // DefaultFilteringSubEntity
        assertThat(entity.defaultEntities, notNullValue());
        assertThat(entity.defaultEntities.size(), is(1));
        final DefaultFilteringSubEntity defaultFilteringSubEntity = entity.defaultEntities.get(0);
        assertThat(defaultFilteringSubEntity.field, is(true));
        assertThat(defaultFilteringSubEntity.getProperty(), is(20L));

        // OneFilteringSubEntity
        assertThat(entity.oneEntities, notNullValue());
        assertThat(entity.oneEntities.size(), is(1));
        final OneFilteringSubEntity oneFilteringSubEntity = entity.oneEntities.get(0);
        assertThat(oneFilteringSubEntity.field1, is(20));
        assertThat(oneFilteringSubEntity.field2, is(30));
        assertThat(oneFilteringSubEntity.getProperty1(), is("property1"));
        assertThat(oneFilteringSubEntity.getProperty2(), is("property2"));

        // ManyFilteringsSubEntity
        assertThat(entity.manyEntities, notNullValue());
        assertThat(entity.manyEntities.size(), is(1));
        final ManyFilteringsSubEntity manyFilteringsSubEntity = entity.manyEntities.get(0);
        assertThat(manyFilteringsSubEntity.field1, is(60));
        assertThat(manyFilteringsSubEntity.field2, is(0));
        assertThat(manyFilteringsSubEntity.getProperty1(), is("property1"));
        assertThat(manyFilteringsSubEntity.getProperty2(), nullValue());
    }

    @Test
    public void testManyFilteringsEntitySecondaryView() throws Exception {
        final ManyFilteringsOnClassEntity entity = target("ManyFilteringsEntitySecondaryView").request()
                .get(ManyFilteringsOnClassEntity.class);

        // ManyFilteringsOnClassEntity
        assertThat(entity.field, is(50));
        assertThat(entity.accessorTransient, is("propertyproperty"));
        assertThat(entity.getProperty(), is("property"));

        // FilteredClassEntity
        final FilteredClassEntity filtered = entity.filtered;
        assertThat(filtered, notNullValue());
        assertThat(filtered.field, is(0));
        assertThat(filtered.getProperty(), nullValue());

        // DefaultFilteringSubEntity
        assertThat(entity.defaultEntities, notNullValue());
        assertThat(entity.defaultEntities.size(), is(1));
        final DefaultFilteringSubEntity defaultFilteringSubEntity = entity.defaultEntities.get(0);
        assertThat(defaultFilteringSubEntity.field, is(true));
        assertThat(defaultFilteringSubEntity.getProperty(), is(20L));

        // OneFilteringSubEntity
        assertThat(entity.oneEntities, notNullValue());
        assertThat(entity.oneEntities.size(), is(1));
        final OneFilteringSubEntity oneFilteringSubEntity = entity.oneEntities.get(0);
        assertThat(oneFilteringSubEntity.field1, is(20));
        assertThat(oneFilteringSubEntity.field2, is(0));
        assertThat(oneFilteringSubEntity.getProperty1(), nullValue());
        assertThat(oneFilteringSubEntity.getProperty2(), is("property2"));

        // ManyFilteringsSubEntity
        assertThat(entity.manyEntities, notNullValue());
        assertThat(entity.manyEntities.size(), is(1));
        final ManyFilteringsSubEntity manyFilteringsSubEntity = entity.manyEntities.get(0);
        assertThat(manyFilteringsSubEntity.field1, is(60));
        assertThat(manyFilteringsSubEntity.field2, is(70));
        assertThat(manyFilteringsSubEntity.getProperty1(), nullValue());
        assertThat(manyFilteringsSubEntity.getProperty2(), is("property2"));
    }

    @Test
    public void testManyFilteringsEntityDefaultView() throws Exception {
        final ManyFilteringsOnClassEntity entity = target("ManyFilteringsEntityDefaultView").request()
                .get(ManyFilteringsOnClassEntity.class);

        // ManyFilteringsOnClassEntity
        assertThat(entity.field, is(0));
        assertThat(entity.accessorTransient, nullValue());
        assertThat(entity.getProperty(), nullValue());

        // FilteredClassEntity
        final FilteredClassEntity filtered = entity.filtered;
        assertThat(filtered, nullValue());

        // DefaultFilteringSubEntity
        assertThat(entity.defaultEntities, nullValue());

        // OneFilteringSubEntity
        assertThat(entity.oneEntities, nullValue());

        // ManyFilteringsSubEntity
        assertThat(entity.manyEntities, nullValue());
    }

    @Test
    public void testManyFilteringsEntityManyViews() throws Exception {
        final ManyFilteringsOnClassEntity entity = target("ManyFilteringsEntityManyViews").request()
                .get(ManyFilteringsOnClassEntity.class);

        // ManyFilteringsOnClassEntity
        assertThat(entity.field, is(50));
        assertThat(entity.accessorTransient, is("propertyproperty"));
        assertThat(entity.getProperty(), is("property"));

        // FilteredClassEntity
        final FilteredClassEntity filtered = entity.filtered;
        assertThat(filtered, notNullValue());
        assertThat(filtered.field, is(0));
        assertThat(filtered.getProperty(), nullValue());

        // DefaultFilteringSubEntity
        assertThat(entity.defaultEntities, notNullValue());
        assertThat(entity.defaultEntities.size(), is(1));
        final DefaultFilteringSubEntity defaultFilteringSubEntity = entity.defaultEntities.get(0);
        assertThat(defaultFilteringSubEntity.field, is(true));
        assertThat(defaultFilteringSubEntity.getProperty(), is(20L));

        // OneFilteringSubEntity
        assertThat(entity.oneEntities, notNullValue());
        assertThat(entity.oneEntities.size(), is(1));
        final OneFilteringSubEntity oneFilteringSubEntity = entity.oneEntities.get(0);
        assertThat(oneFilteringSubEntity.field1, is(20));
        assertThat(oneFilteringSubEntity.field2, is(30));
        assertThat(oneFilteringSubEntity.getProperty1(), is("property1"));
        assertThat(oneFilteringSubEntity.getProperty2(), is("property2"));

        // ManyFilteringsSubEntity
        assertThat(entity.manyEntities, notNullValue());
        assertThat(entity.manyEntities.size(), is(1));
        final ManyFilteringsSubEntity manyFilteringsSubEntity = entity.manyEntities.get(0);
        assertThat(manyFilteringsSubEntity.field1, is(60));
        assertThat(manyFilteringsSubEntity.field2, is(70));
        assertThat(manyFilteringsSubEntity.getProperty1(), is("property1"));
        assertThat(manyFilteringsSubEntity.getProperty2(), is("property2"));
    }
}
