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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.ManyFilteringsOnClassEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.OneFilteringOnClassEntity;

import org.junit.Test;

/**
 * Use-cases with entity-filtering annotations on class.
 *
 * @author Michal Gajdos
 */
public class EntityFilteringOnClassTest extends EntityFilteringTest {

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig()
                // Resources.
                .register(Resource.class)
                // Providers.
                .register(EntityFilteringFeature.class)
                .register(FilteringMessageBodyProvider.class);
    }

    @Path("/")
    @Consumes("entity/filtering")
    @Produces("entity/filtering")
    public static class Resource {

        @GET
        @Path("OneFilteringEntity")
        @PrimaryDetailedView
        public OneFilteringOnClassEntity getOneFilteringEntity() {
            return new OneFilteringOnClassEntity();
        }

        @GET
        @Path("OneFilteringEntityDefaultView")
        public OneFilteringOnClassEntity getOneFilteringEntityDefaultView() {
            return new OneFilteringOnClassEntity();
        }

        @POST
        @Path("OneFilteringEntity")
        public String postOneFilteringEntity(final String value) {
            return value;
        }

        @GET
        @Path("OneFilteringEntityDefaultViewResponse")
        public Response getOneFilteringEntityDefaultViewResponse() {
            return Response.ok().entity(new OneFilteringOnClassEntity(), new Annotation[] {new DefaultFilteringScope()}).build();
        }

        @GET
        @Path("ManyFilteringsEntityPrimaryView")
        @PrimaryDetailedView
        public ManyFilteringsOnClassEntity getManyFilteringsEntityPrimaryView() {
            return new ManyFilteringsOnClassEntity();
        }

        @GET
        @Path("ManyFilteringsEntitySecondaryView")
        @SecondaryDetailedView
        public ManyFilteringsOnClassEntity getManyFilteringsEntitySecondaryView() {
            return new ManyFilteringsOnClassEntity();
        }

        @GET
        @Path("ManyFilteringsEntityDefaultView")
        public ManyFilteringsOnClassEntity getManyFilteringsEntityDefaultView() {
            return new ManyFilteringsOnClassEntity();
        }

        @GET
        @Path("ManyFilteringsEntityManyViews")
        @PrimaryDetailedView
        @SecondaryDetailedView
        public ManyFilteringsOnClassEntity getManyFilteringsEntityManyViews() {
            return new ManyFilteringsOnClassEntity();
        }
    }

    @Test
    public void testOneEntityFilteringOnClass() throws Exception {
        final String fields = target("OneFilteringEntity").request().get(String.class);

        assertSameFields(fields, "field,accessor,property,subEntities.field2,subEntities.property2,subEntities.property1,"
                + "subEntities.field1,defaultEntities.field,defaultEntities.property");
    }

    @Test
    public void testOneEntityFilteringOnClassDefaultViewResponse() throws Exception {
        final String fields = target("OneFilteringEntityDefaultViewResponse").request().get(String.class);

        assertSameFields(fields, "");
    }

    @Test
    public void testOneEntityFilteringOnClassDefaultView() throws Exception {
        final String fields = target("OneFilteringEntityDefaultView").request().get(String.class);

        assertSameFields(fields, "");
    }

    @Test
    public void testMultipleViewsOnClass() throws Exception {
        testOneEntityFilteringOnClass();
        testOneEntityFilteringOnClassDefaultView();
    }

    @Test
    public void testManyFilteringsEntityPrimaryView() throws Exception {
        final String fields = target("ManyFilteringsEntityPrimaryView").request().get(String.class);

        assertSameFields(fields, "field,accessor,property,manyEntities.property1,manyEntities.field1,oneEntities.field2,"
                + "oneEntities.property2,oneEntities.property1,oneEntities.field1,defaultEntities.field,defaultEntities"
                + ".property");
    }

    @Test
    public void testManyFilteringsEntitySecondaryView() throws Exception {
        final String fields = target("ManyFilteringsEntitySecondaryView").request().get(String.class);

        assertSameFields(fields, "field,accessor,property,manyEntities.field2,manyEntities.property2,manyEntities.field1,"
                + "oneEntities.property2,oneEntities.field1,defaultEntities.field,defaultEntities.property");
    }

    @Test
    public void testManyFilteringsEntityDefaultView() throws Exception {
        final String fields = target("ManyFilteringsEntityDefaultView").request().get(String.class);

        assertSameFields(fields, "");
    }

    @Test
    public void testManyFilteringsEntityManyViews() throws Exception {
        final String fields = target("ManyFilteringsEntityManyViews").request().get(String.class);

        assertSameFields(fields, "field,accessor,property,manyEntities.field2,manyEntities.property2,manyEntities.property1,"
                + "manyEntities.field1,oneEntities.field2,oneEntities.property2,oneEntities.property1,oneEntities.field1,"
                + "defaultEntities.field,defaultEntities.property");
    }
}
