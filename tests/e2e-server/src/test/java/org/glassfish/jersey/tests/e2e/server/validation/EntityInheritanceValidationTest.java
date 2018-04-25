/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.validation;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.hibernate.validator.constraints.NotBlank;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests to ensure that validation constraints on a superclass are validated as well.
 *
 * @author Michal Gajdos
 */
public class EntityInheritanceValidationTest extends JerseyTest {

    @Path("/")
    public static class Resource {

        @POST
        @Produces("application/json")
        public Entity post(@Valid final Entity entity) {
            return entity;
        }
    }

    public static class AbstractEntity {

        private String text;

        public AbstractEntity() {
        }

        public AbstractEntity(final String text) {
            this.text = text;
        }

        @NotNull
        @NotBlank
        public String getText() {
            return text;
        }

        public void setText(final String text) {
            this.text = text;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final AbstractEntity that = (AbstractEntity) o;

            if (!text.equals(that.text)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return text.hashCode();
        }
    }

    public static class Entity extends AbstractEntity {

        private Integer number;

        public Entity() {
        }

        public Entity(final String text, final Integer number) {
            super(text);
            this.number = number;
        }

        @Min(12)
        @Max(14)
        @NotNull
        public Integer getNumber() {
            return number;
        }

        public void setNumber(final Integer number) {
            this.number = number;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            final Entity entity = (Entity) o;

            if (!number.equals(entity.number)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + number.hashCode();
            return result;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class)
                .register(JacksonFeature.class);
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        config.register(JacksonFeature.class);
    }

    @Test
    public void testEntityInheritance() throws Exception {
        final Entity entity = new Entity("foo", 13);
        final Response response = target().request().post(javax.ws.rs.client.Entity.json(entity));

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(Entity.class), is(entity));
    }

    @Test
    public void testEntityInheritanceBlankText() throws Exception {
        final Response response = target().request().post(javax.ws.rs.client.Entity.json(new Entity("", 13)));

        assertThat(response.getStatus(), is(400));
    }

    @Test
    public void testEntityInheritanceInvalidNumber() throws Exception {
        final Response response = target().request().post(javax.ws.rs.client.Entity.json(new Entity("foo", 23)));

        assertThat(response.getStatus(), is(400));
    }
}
