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
import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.EmptyEntity;
import org.glassfish.jersey.tests.e2e.entity.filtering.domain.NonEmptyEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Use-cases to check whether empty class causes problems (JERSEY-2824 reproducer).
 *
 * @author Michal Gajdos
 */
@RunWith(Parameterized.class)
public class JsonEmptyEntityTest extends JerseyTest {

    @Parameterized.Parameters(name = "Provider: {0}")
    public static Iterable<Class[]> providers() {
        return Arrays.asList(new Class[][] {{MoxyJsonFeature.class}, {JacksonFeature.class}});
    }

    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
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

    public JsonEmptyEntityTest(final Class<Feature> filteringProvider) {
        super(new ResourceConfig(Resource.class, EntityFilteringFeature.class)
                .register(filteringProvider)
                .register(new ContextResolver<ObjectMapper>() {
                    @Override
                    public ObjectMapper getContext(final Class<?> type) {
                        return new ObjectMapper()
                                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    }
                }));

        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);
    }

    @Test
    public void testNonEmptyEntity() throws Exception {
        final NonEmptyEntity entity = target("nonEmptyEntity").request().get(NonEmptyEntity.class);

        assertThat(entity.getValue(), is("foo"));
        assertThat(entity.getEmptyEntity(), nullValue());
    }

    @Test
    public void testEmptyEntity() throws Exception {
        assertThat(target("emptyEntity").request().get(String.class), is("{}"));
    }
}
