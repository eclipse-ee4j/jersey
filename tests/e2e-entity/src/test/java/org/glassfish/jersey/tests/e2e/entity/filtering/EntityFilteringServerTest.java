/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.entity.filtering;

import java.lang.annotation.Annotation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.ManyFilteringsOnClassEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Michal Gajdos
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        EntityFilteringServerTest.ConfigurationServerTest.class,
        EntityFilteringServerTest.ConfigurationDefaultViewServerTest.class,
        EntityFilteringServerTest.AnnotationsServerTest.class,
        EntityFilteringServerTest.AnnotationsOverConfigurationServerTest.class
})
public class EntityFilteringServerTest {

    @Path("/")
    @Produces("entity/filtering")
    public static class Resource {

        @GET
        @Path("configuration")
        public ManyFilteringsOnClassEntity getConfiguration() {
            return new ManyFilteringsOnClassEntity();
        }

        @GET
        @Path("configurationOverResource")
        @SecondaryDetailedView
        public ManyFilteringsOnClassEntity getConfigurationOverResource() {
            return new ManyFilteringsOnClassEntity();
        }

        @GET
        @Path("annotations")
        public Response getAnnotations() {
            return Response
                    .ok()
                    .entity(new ManyFilteringsOnClassEntity(), new Annotation[] {PrimaryDetailedView.Factory.get()})
                    .build();
        }

        @GET
        @Path("annotationsOverConfiguration")
        public Response getAnnotationsOverConfiguration() {
            return Response
                    .ok()
                    .entity(new ManyFilteringsOnClassEntity(), new Annotation[] {PrimaryDetailedView.Factory.get()})
                    .build();
        }

        @GET
        @Path("annotationsOverResource")
        @SecondaryDetailedView
        public Response getAnnotationsOverResource() {
            return Response
                    .ok()
                    .entity(new ManyFilteringsOnClassEntity(), new Annotation[] {PrimaryDetailedView.Factory.get()})
                    .build();
        }

        @GET
        @Path("annotationsOverConfigurationOverResource")
        @SecondaryDetailedView
        public Response getAnnotationsOverConfigurationOverResource() {
            return Response
                    .ok()
                    .entity(new ManyFilteringsOnClassEntity(), new Annotation[] {PrimaryDetailedView.Factory.get()})
                    .build();
        }
    }

    private static class FilteringResourceConfig extends ResourceConfig {

        private FilteringResourceConfig() {
            // Resources.
            register(Resource.class);

            // Providers.
            register(EntityFilteringFeature.class);
            register(FilteringMessageBodyProvider.class);
        }
    }

    public static class ConfigurationServerTest extends EntityFilteringTest {

        @Override
        protected Application configure() {
            enable(TestProperties.DUMP_ENTITY);
            enable(TestProperties.LOG_TRAFFIC);

            return new FilteringResourceConfig()
                    // Properties
                    .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE, PrimaryDetailedView.Factory.get());
        }

        @Test
        public void testConfiguration() throws Exception {
            final String fields = target("configuration").request().get(String.class);

            assertSameFields(fields, "field,accessor,property,manyEntities.property1,manyEntities.field1,oneEntities.field2,"
                    + "oneEntities.property2,oneEntities.property1,oneEntities.field1,defaultEntities.field,defaultEntities"
                    + ".property");
        }

        @Test
        public void testConfigurationOverResource() throws Exception {
            final String fields = target("configurationOverResource").request().get(String.class);

            assertSameFields(fields, "field,accessor,property,manyEntities.property1,manyEntities.field1,oneEntities.field2,"
                    + "oneEntities.property2,oneEntities.property1,oneEntities.field1,defaultEntities.field,defaultEntities"
                    + ".property");
        }
    }

    public static class ConfigurationDefaultViewServerTest extends EntityFilteringTest {

        @Override
        protected Application configure() {
            enable(TestProperties.DUMP_ENTITY);
            enable(TestProperties.LOG_TRAFFIC);

            return new FilteringResourceConfig();
        }

        @Test
        public void testConfiguration() throws Exception {
            final String fields = target("configuration").request().get(String.class);

            assertSameFields(fields, "");
        }
    }

    public static class AnnotationsServerTest extends EntityFilteringTest {

        @Override
        protected Application configure() {
            enable(TestProperties.DUMP_ENTITY);
            enable(TestProperties.LOG_TRAFFIC);

            return new FilteringResourceConfig();
        }

        @Test
        public void testAnnotations() throws Exception {
            final String fields = target("annotations").request().get(String.class);

            assertSameFields(fields, "field,accessor,property,manyEntities.property1,manyEntities.field1,oneEntities.field2,"
                    + "oneEntities.property2,oneEntities.property1,oneEntities.field1,defaultEntities.field,defaultEntities"
                    + ".property");
        }

        @Test
        public void testAnnotationsOverResource() throws Exception {
            final String fields = target("annotationsOverResource").request().get(String.class);

            assertSameFields(fields, "field,accessor,property,manyEntities.property1,manyEntities.field1,oneEntities.field2,"
                    + "oneEntities.property2,oneEntities.property1,oneEntities.field1,defaultEntities.field,defaultEntities"
                    + ".property");
        }
    }

    public static class AnnotationsOverConfigurationServerTest extends EntityFilteringTest {

        @Override
        protected Application configure() {
            enable(TestProperties.DUMP_ENTITY);
            enable(TestProperties.LOG_TRAFFIC);

            return new FilteringResourceConfig()
                    // Properties
                    .property(EntityFilteringFeature.ENTITY_FILTERING_SCOPE, new DefaultFilteringScope());
        }

        @Test
        public void testAnnotationsOverConfiguration() throws Exception {
            final String fields = target("annotationsOverConfiguration").request().get(String.class);

            assertSameFields(fields, "field,accessor,property,manyEntities.property1,manyEntities.field1,oneEntities.field2,"
                    + "oneEntities.property2,oneEntities.property1,oneEntities.field1,defaultEntities.field,defaultEntities"
                    + ".property");
        }

        @Test
        public void testAnnotationsOverConfigurationOverResource() throws Exception {
            final String fields = target("annotationsOverConfigurationOverResource").request().get(String.class);

            assertSameFields(fields, "field,accessor,property,manyEntities.property1,manyEntities.field1,oneEntities.field2,"
                    + "oneEntities.property2,oneEntities.property1,oneEntities.field1,defaultEntities.field,defaultEntities"
                    + ".property");
        }
    }
}
