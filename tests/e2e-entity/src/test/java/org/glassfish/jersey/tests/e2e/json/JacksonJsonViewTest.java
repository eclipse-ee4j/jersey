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

package org.glassfish.jersey.tests.e2e.json;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.inject.Singleton;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * Reproducer for JERSEY-1878.
 *
 * @author Michal Gajdos
 */
public class JacksonJsonViewTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class, JacksonFeature.class);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(JacksonFeature.class);
    }

    public static class SimpleView {}
    public static class DetailedView {}

    public static class TestEntity {

        public static final TestEntity ENTITY = new TestEntity("simple", "detailed");
        public static final TestEntity DETAILED = new TestEntity(null, "detailed");

        private String simple;
        private String detailed;

        public TestEntity() {
        }

        public TestEntity(final String simple, final String detailed) {
            this.simple = simple;
            this.detailed = detailed;
        }

        @JsonView(SimpleView.class)
        public String getSimple() {
            return simple;
        }

        @JsonView(DetailedView.class)
        public String getDetailed() {
            return detailed;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final TestEntity that = (TestEntity) o;

            if (detailed != null ? !detailed.equals(that.detailed) : that.detailed != null) {
                return false;
            }
            if (simple != null ? !simple.equals(that.simple) : that.simple != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = simple != null ? simple.hashCode() : 0;
            result = 31 * result + (detailed != null ? detailed.hashCode() : 0);
            return result;
        }
    }

    @Path("/")
    @Singleton
    public static class MyResource {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("async")
        @JsonView(DetailedView.class)
        public void getAsync(@Suspended final AsyncResponse response) {
            response.resume(TestEntity.ENTITY);
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("sync")
        @JsonView(DetailedView.class)
        public TestEntity getSync() {
            return TestEntity.ENTITY;
        }
    }

    @Test
    public void testSync() throws Exception {
        final Response response = target().path("sync").request().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(TestEntity.class), is(TestEntity.DETAILED));
    }

    @Test
    public void testAsync() throws Exception {
        final Response response = target().path("async").request().async().get().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(TestEntity.class), is(TestEntity.DETAILED));

        response.close();
    }
}
