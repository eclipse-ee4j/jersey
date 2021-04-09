/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.entity;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.util.runner.ConcurrentRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RunWith(ConcurrentRunner.class)
public class EnumEntityTest extends JerseyTest {

    public enum SimpleEnum {
        VALUE1,
        VALUE2
    }

    public enum ValueEnum {
        VALUE100(100),
        VALUE200(200);

        private final int value;

        ValueEnum(int value) {
            this.value = value;
        }
    }

    @Path("/")
    public static class EnumResource {
        @POST
        @Path("/simple")
        public String postSimple(SimpleEnum simpleEnum) {
            return simpleEnum.name();
        }

        @POST
        @Path("/value")
        public String postValue(ValueEnum valueEnum) {
            return valueEnum.name();
        }

        @POST
        @Path("/echo")
        public String echo(String value) {
            return value;
        }

        @PUT
        @Path("/simple")
        public SimpleEnum putSimple(String simple) {
            return SimpleEnum.valueOf(simple);
        }

        @PUT
        @Path("value")
        public ValueEnum putValue(String value) {
            return ValueEnum.valueOf(value);
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(EnumResource.class);
    }

    // Server side tests

    @Test
    public void testSimpleEnumServerReader() {
        for (SimpleEnum value : SimpleEnum.values()) {
            try (Response r = target("simple").request(MediaType.TEXT_PLAIN_TYPE)
                    .post(Entity.entity(value.name(), MediaType.TEXT_PLAIN_TYPE))) {
                Assert.assertEquals(value.name(), r.readEntity(String.class));
            }
        }
    }

    @Test
    public void testValueEnumServerReader() {
        for (ValueEnum value : ValueEnum.values()) {
            try (Response r = target("value").request(MediaType.TEXT_PLAIN_TYPE)
                    .post(Entity.entity(value.name(), MediaType.TEXT_PLAIN_TYPE))) {
                Assert.assertEquals(value.name(), r.readEntity(String.class));
            }
        }
    }

    @Test
    public void testSimpleEnumServerWriter() {
        for (SimpleEnum value : SimpleEnum.values()) {
            try (Response r = target("simple").request(MediaType.TEXT_PLAIN_TYPE)
                    .put(Entity.entity(value.name(), MediaType.TEXT_PLAIN_TYPE))) {
                Assert.assertEquals(value.name(), r.readEntity(String.class));
            }
        }
    }

    @Test
    public void testValueEnumServerWriter() {
        for (ValueEnum value : ValueEnum.values()) {
            try (Response r = target("value").request(MediaType.TEXT_PLAIN_TYPE)
                    .put(Entity.entity(value.name(), MediaType.TEXT_PLAIN_TYPE))) {
                Assert.assertEquals(value.name(), r.readEntity(String.class));
            }
        }
    }

    // Client side tests

    @Test
    public void testSimpleEnumClientReader() {
        for (SimpleEnum value : SimpleEnum.values()) {
            try (Response r = target("simple").request(MediaType.TEXT_PLAIN_TYPE)
                    .post(Entity.entity(value.name(), MediaType.TEXT_PLAIN_TYPE))) {
                Assert.assertEquals(value, r.readEntity(SimpleEnum.class));
            }
        }
    }

    @Test
    public void testValueEnumClientReader() {
        for (ValueEnum value : ValueEnum.values()) {
            try (Response r = target("value").request(MediaType.TEXT_PLAIN_TYPE)
                    .post(Entity.entity(value.name(), MediaType.TEXT_PLAIN_TYPE))) {
                Assert.assertEquals(value, r.readEntity(ValueEnum.class));
            }
        }
    }

    @Test
    public void testSimpleEnumClientWriter() {
        for (SimpleEnum value : SimpleEnum.values()) {
            try (Response r = target("echo").request(MediaType.TEXT_PLAIN_TYPE)
                    .post(Entity.entity(value, MediaType.TEXT_PLAIN_TYPE))) {
                Assert.assertEquals(value.name(), r.readEntity(String.class));
            }
        }
    }

    @Test
    public void testValueEnumClientWriter() {
        for (ValueEnum value : ValueEnum.values()) {
            try (Response r = target("echo").request(MediaType.TEXT_PLAIN_TYPE)
                    .post(Entity.entity(value, MediaType.TEXT_PLAIN_TYPE))) {
                Assert.assertEquals(value.name(), r.readEntity(String.class));
            }
        }
    }

}
