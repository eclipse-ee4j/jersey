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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.inject.CustomAnnotationLiteral;
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
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class JsonEntityFilteringClientTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][] {{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    @Parameterized.Parameter
    public Class<Feature> filteringProvider;

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig()
                // Resources.
                .register(Resource.class);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(EntityFilteringFeature.class).register(filteringProvider);
    }

    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    public static class Resource {

        @POST
        public String post(final String value) {
            return value;
        }
    }

    @Test
    public void testEntityAnnotationsPrimaryView() throws Exception {
        final OneFilteringOnClassEntity entity = target()
                .request()
                .post(Entity.entity(
                                OneFilteringOnClassEntity.INSTANCE,
                                MediaType.APPLICATION_JSON_TYPE,
                                new Annotation[] {PrimaryDetailedView.Factory.get()}),
                        OneFilteringOnClassEntity.class);

        _testPrimaryViewEntity(entity);
    }

    @Test
    public void testEntityAnnotationsDefaultView() throws Exception {
        final OneFilteringOnClassEntity entity = target()
                .request()
                .post(Entity.entity(
                                OneFilteringOnClassEntity.INSTANCE,
                                MediaType.APPLICATION_JSON_TYPE,
                                new Annotation[] {new DefaultFilteringScope()}),
                        OneFilteringOnClassEntity.class);

        _testEmptyEntity(entity);
    }

    @Test
    public void testEntityAnnotationsInvalidView() throws Exception {
        final OneFilteringOnClassEntity entity = target()
                .request()
                .post(Entity.entity(
                                OneFilteringOnClassEntity.INSTANCE,
                                MediaType.APPLICATION_JSON_TYPE,
                                new Annotation[] {CustomAnnotationLiteral.INSTANCE}),
                        OneFilteringOnClassEntity.class);

        _testEmptyEntity(entity);
    }

    @Test
    public void testConfigurationPrimaryView() throws Exception {
        _testPrimaryViewEntity(retrieveEntity(PrimaryDetailedView.Factory.get()));
    }

    @Test
    public void testConfigurationDefaultView() throws Exception {
        _testEmptyEntity(retrieveEntity(new DefaultFilteringScope()));
    }

    @Test
    public void testConfigurationMultipleViews() throws Exception {
        _testPrimaryViewEntity(retrieveEntity(PrimaryDetailedView.Factory.get(), CustomAnnotationLiteral.INSTANCE));
    }

    private OneFilteringOnClassEntity retrieveEntity(final Annotation... annotations) {
        final ClientConfig config = new ClientConfig()
                .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE, annotations.length == 1 ? annotations[0] : annotations);
        configureClient(config);

        return ClientBuilder.newClient(config)
                .target(getBaseUri())
                .request()
                .post(Entity.entity(OneFilteringOnClassEntity.INSTANCE, MediaType.APPLICATION_JSON_TYPE),
                        OneFilteringOnClassEntity.class);
    }

    @Test
    public void testInvalidConfiguration() throws Exception {
        final ClientConfig config = new ClientConfig()
                .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE, "invalid_value");
        configureClient(config);

        final OneFilteringOnClassEntity entity =
                ClientBuilder.newClient(config)
                        .target(getBaseUri())
                        .request()
                        .post(Entity.entity(OneFilteringOnClassEntity.INSTANCE, MediaType.APPLICATION_JSON_TYPE),
                                OneFilteringOnClassEntity.class);

        _testEmptyEntity(entity);
    }

    @Test
    public void testEntityAnnotationsOverConfiguration() throws Exception {
        final ClientConfig config = new ClientConfig()
                .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE, SecondaryDetailedView.Factory.get());
        configureClient(config);

        final ManyFilteringsOnClassEntity entity = ClientBuilder.newClient(config)
                .target(getBaseUri())
                .request()
                .post(Entity.entity(
                                ManyFilteringsOnClassEntity.INSTANCE,
                                MediaType.APPLICATION_JSON_TYPE,
                                new Annotation[] {PrimaryDetailedView.Factory.get()}),
                        ManyFilteringsOnClassEntity.class);

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

    private void _testEmptyEntity(final OneFilteringOnClassEntity entity) {
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

    private void _testPrimaryViewEntity(final OneFilteringOnClassEntity entity) {
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
}
