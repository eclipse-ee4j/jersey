/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import javax.inject.Inject;
import javax.inject.Named;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests that HK2Binder and Jersey Binder work together.
 *
 * @author Petr Bouda
 */
public class Hk2BinderSupportTest extends JerseyTest {

    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig();
        config.register(HelloWorldResource.class);
        config.register(new Hk2Binder());
        config.register(new JerseyBinder());
        return config;
    }

    @Test
    public void testResponse() {
        String s = target().path("helloworld").request().get(String.class);
        assertEquals(Hk2Binder.HK2_HELLO_MESSAGE + "/" + JerseyBinder.JERSEY_HELLO_MESSAGE, s);
    }

    private static class Hk2Binder extends AbstractBinder {
        private static final String HK2_HELLO_MESSAGE = "Hello HK2!";

        @Override
        protected void configure() {
            bind(HK2_HELLO_MESSAGE).to(String.class).named("hk2");
        }
    }

    private static class JerseyBinder extends org.glassfish.jersey.internal.inject.AbstractBinder {
        private static final String JERSEY_HELLO_MESSAGE = "Hello Jersey!";

        @Override
        protected void configure() {
            bind(JERSEY_HELLO_MESSAGE).to(String.class).named("jersey");
        }
    }

    @Path("helloworld")
    public static class HelloWorldResource {

        @Inject
        @Named("hk2")
        private String hk2Hello;

        @Inject
        @Named("jersey")
        private String jerseyHello;

        @GET
        public String getHello() {
            return hk2Hello + "/" + jerseyHello;
        }
    }
}
