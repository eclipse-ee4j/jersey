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

import javax.ws.rs.GET;
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
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.OneFilteringSubEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        JsonEntityFilteringServerTest.ConfigurationServerTest.class,
        JsonEntityFilteringServerTest.ConfigurationDefaultViewServerTest.class,
        JsonEntityFilteringServerTest.AnnotationsServerTest.class,
        JsonEntityFilteringServerTest.AnnotationsOverConfigurationServerTest.class
})
public class JsonEntityFilteringServerTest {

    @Path("/")
    @Produces("application/json")
    public static class Resource {

        @GET
        @Path("configuration")
        public ManyFilteringsOnClassEntity getConfiguration() {
            return ManyFilteringsOnClassEntity.INSTANCE;
        }

        @GET
        @Path("configurationOverResource")
        @SecondaryDetailedView
        public ManyFilteringsOnClassEntity getConfigurationOverResource() {
            return ManyFilteringsOnClassEntity.INSTANCE;
        }

        @GET
        @Path("annotations")
        public Response getAnnotations() {
            return Response
                    .ok()
                    .entity(ManyFilteringsOnClassEntity.INSTANCE, new Annotation[] {PrimaryDetailedView.Factory.get()})
                    .build();
        }

        @GET
        @Path("annotationsOverConfiguration")
        public Response getAnnotationsOverConfiguration() {
            return Response
                    .ok()
                    .entity(ManyFilteringsOnClassEntity.INSTANCE, new Annotation[] {PrimaryDetailedView.Factory.get()})
                    .build();
        }

        @GET
        @Path("annotationsOverResource")
        @SecondaryDetailedView
        public Response getAnnotationsOverResource() {
            return Response
                    .ok()
                    .entity(ManyFilteringsOnClassEntity.INSTANCE, new Annotation[] {PrimaryDetailedView.Factory.get()})
                    .build();
        }

        @GET
        @Path("annotationsOverConfigurationOverResource")
        @SecondaryDetailedView
        public Response getAnnotationsOverConfigurationOverResource() {
            return Response
                    .ok()
                    .entity(ManyFilteringsOnClassEntity.INSTANCE, new Annotation[] {PrimaryDetailedView.Factory.get()})
                    .build();
        }
    }

    @RunWith(Parameterized.class)
    public static class ConfigurationServerTest extends JerseyTest {

        @Parameterized.Parameters(name = "Provider: {0}")
        public static Iterable<Class[]> providers() {
            return Arrays.asList(new Class[][] {{MoxyJsonFeature.class}, {JacksonFeature.class}});
        }

        public ConfigurationServerTest(final Class<Feature> filteringProvider) {
            super(new ResourceConfig(Resource.class, EntityFilteringFeature.class)
                    .register(filteringProvider)
                    .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE, PrimaryDetailedView.Factory.get()));

            enable(TestProperties.DUMP_ENTITY);
            enable(TestProperties.LOG_TRAFFIC);
        }

        @Test
        public void testConfiguration() throws Exception {
            _testEntity(target("configuration").request().get(ManyFilteringsOnClassEntity.class));
        }

        @Test
        public void testConfigurationOverResource() throws Exception {
            _testEntity(target("configurationOverResource").request().get(ManyFilteringsOnClassEntity.class));
        }
    }

    @RunWith(Parameterized.class)
    public static class ConfigurationDefaultViewServerTest extends JerseyTest {

        @Parameterized.Parameters(name = "Provider: {0}")
        public static Iterable<Class[]> providers() {
            return Arrays.asList(new Class[][] {{MoxyJsonFeature.class}, {JacksonFeature.class}});
        }

        public ConfigurationDefaultViewServerTest(final Class<Feature> filteringProvider) {
            super(new ResourceConfig(Resource.class, EntityFilteringFeature.class).register(filteringProvider));

            enable(TestProperties.DUMP_ENTITY);
            enable(TestProperties.LOG_TRAFFIC);
        }

        @Test
        public void testConfiguration() throws Exception {
            final ManyFilteringsOnClassEntity entity = target("configuration").request().get(ManyFilteringsOnClassEntity.class);

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
    }

    @RunWith(Parameterized.class)
    public static class AnnotationsServerTest extends JerseyTest {

        @Parameterized.Parameters(name = "Provider: {0}")
        public static Iterable<Class[]> providers() {
            return Arrays.asList(new Class[][] {{MoxyJsonFeature.class}, {JacksonFeature.class}});
        }

        public AnnotationsServerTest(final Class<Feature> filteringProvider) {
            super(new ResourceConfig(Resource.class, EntityFilteringFeature.class).register(filteringProvider));

            enable(TestProperties.DUMP_ENTITY);
            enable(TestProperties.LOG_TRAFFIC);
        }

        @Test
        public void testAnnotations() throws Exception {
            _testEntity(target("annotations").request().get(ManyFilteringsOnClassEntity.class));
        }

        @Test
        public void testAnnotationsOverResource() throws Exception {
            _testEntity(target("annotationsOverResource").request().get(ManyFilteringsOnClassEntity.class));
        }
    }

    @RunWith(Parameterized.class)
    public static class AnnotationsOverConfigurationServerTest extends JerseyTest {

        @Parameterized.Parameters(name = "Provider: {0}")
        public static Iterable<Class[]> providers() {
            return Arrays.asList(new Class[][] {{MoxyJsonFeature.class}, {JacksonFeature.class}});
        }

        public AnnotationsOverConfigurationServerTest(final Class<Feature> filteringProvider) {
            super(new ResourceConfig(Resource.class, EntityFilteringFeature.class)
                    .register(filteringProvider)
                    .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE, new DefaultFilteringScope()));

            enable(TestProperties.DUMP_ENTITY);
            enable(TestProperties.LOG_TRAFFIC);
        }

        @Test
        public void testAnnotationsOverConfiguration() throws Exception {
            _testEntity(target("annotationsOverConfiguration").request().get(ManyFilteringsOnClassEntity.class));
        }

        @Test
        public void testAnnotationsOverConfigurationOverResource() throws Exception {
            _testEntity(target("annotationsOverConfigurationOverResource").request().get(ManyFilteringsOnClassEntity.class));
        }
    }

    private static void _testEntity(final ManyFilteringsOnClassEntity entity) {
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
}
