/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ExternalPropertiesConfigurationFactoryTest extends JerseyTest {

    private static final ApplicationPropertiesConfig applicationPropertiesConfig = new ApplicationPropertiesConfig();

    private static class ApplicationPropertiesConfig extends ResourceConfig {

        public ApplicationPropertiesConfig() {
            register(MyResource.class);
        }
    }

    @Path("/")
    @Singleton
    public static class MyResource {


        @GET
        @Path("readProperty/{key}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response readProperties(@PathParam("key") String key) {
            return Response.ok(String.valueOf(applicationPropertiesConfig.getProperty(key))).build();
        }

        @GET
        @Path("getPropertyValue/{key}")
        @Produces(MediaType.WILDCARD)
        public Boolean getPropertyValue(@PathParam("key") String key) {
            final Object value = applicationPropertiesConfig.getProperty(key);
            return value == null ? null : Boolean.valueOf(value.toString());
        }

    }


    @Override
    protected Application configure() {

        return applicationPropertiesConfig;

    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new JettyTestContainerFactory();
    }

    @Test
    public void readConfigTest() {

        final Boolean responce = target("getPropertyValue/{key}")
                .resolveTemplate("key", "jersey.config.disableMetainfServicesLookup").request().get(Boolean.class);
        Assert.assertEquals(Boolean.TRUE, responce);
    }

    @Test
    public void smallRyeConfigTest() {

        final String responce = target("readProperty/{key}")
                .resolveTemplate("key", "jersey.config.disableAutoDiscovery").request().get(String.class);
        Assert.assertEquals("1", responce);
    }

    @Test
    public void defaultHeaderValueTest() {
        final String responce = target("readProperty/{key}")
                .resolveTemplate("key", "jersey.config.disableJsonProcessing").request().get(String.class);
        Assert.assertEquals("true", responce);
    }
}