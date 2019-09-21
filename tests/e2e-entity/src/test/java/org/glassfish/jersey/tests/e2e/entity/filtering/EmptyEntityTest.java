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

package org.glassfish.jersey.tests.e2e.entity.filtering;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.EmptyEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.NonEmptyEntity;

import org.junit.Test;

/**
 * Use-cases to check whether empty class causes problems (JERSEY-2824 reproducer).
 *
 * @author Michal Gajdos
 */
public class EmptyEntityTest extends EntityFilteringTest {

    @Path("/")
    @Consumes("entity/filtering")
    @Produces("entity/filtering")
    public static class Resource {

        @Path("nonEmptyEntity")
        @GET
        public NonEmptyEntity nonEmptyEntity() {
            return NonEmptyEntity.INSTANCE;
        }

        @Path("emptyEntity")
        @GET
        public EmptyEntity emptyEntity() {
            return new EmptyEntity();
        }
    }

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

    @Test
    public void testNonEmptyEntity() throws Exception {
        final String fields = target("nonEmptyEntity").request().get(String.class);

        assertSameFields(fields, "value");
    }

    @Test
    public void testEmptyEntity() throws Exception {
        final String fields = target("emptyEntity").request().get(String.class);

        assertSameFields(fields, "");
    }
}
