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

package org.glassfish.jersey.tests.integration.jersey1883;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class Jersey1883ITCase extends JerseyTest {

    @Before
    public void setup() {
        Assume.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
    }

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testGetRestNoLife() throws Exception {
        Response response = target("rest1").path("no-life").request().get();
        assertThat(response.readEntity(String.class), equalTo("ciao #1"));

        response = target("rest1").path("no-life").request().get();
        assertThat(response.readEntity(String.class), equalTo("ciao #1"));

        response = target("rest1").path("no-life").request().get();
        assertThat(response.readEntity(String.class), equalTo("ciao #1"));

        response = target("rest1").path("no-life").request().get();
        assertThat(response.readEntity(String.class), equalTo("ciao #1"));
    }

    @Test
    public void testGetRestSingletonLife() throws Exception {
        Response response = target("rest2").path("singleton-life").request().get();
        assertThat(response.readEntity(String.class), equalTo("hello #1"));

        response = target("rest2").path("singleton-life").request().get();
        assertThat(response.readEntity(String.class), equalTo("hello #1"));

        response = target("rest2").path("singleton-life").request().get();
        assertThat(response.readEntity(String.class), equalTo("hello #1"));

        response = target("rest2").path("singleton-life").request().get();
        assertThat(response.readEntity(String.class), equalTo("hello #1"));
    }

    @Test
    public void testGetRestLife() throws Exception {
        Response response = target("rest3").path("life").request().get();
        assertThat(response.readEntity(String.class), equalTo("hi #2"));

        response = target("rest3").path("life").request().get();
        assertThat(response.readEntity(String.class), equalTo("hi #3"));

        response = target("rest3").path("life").request().get();
        assertThat(response.readEntity(String.class), equalTo("hi #4"));

        response = target("rest3").path("life").request().get();
        assertThat(response.readEntity(String.class), equalTo("hi #5"));
    }

}
