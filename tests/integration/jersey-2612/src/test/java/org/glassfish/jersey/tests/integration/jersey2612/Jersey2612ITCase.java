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

package org.glassfish.jersey.tests.integration.jersey2612;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Reproducer tests for JERSEY-2612.
 */
public class Jersey2612ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new Jersey2612();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testResourceMethodWithOptionalGivenNoQueryParam() throws Exception {
        final Response response = target("/hello").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("Hello World!"));
    }

    @Test
    public void testResourceMethodWithOptionalGivenQueryParam() throws Exception {
        final Response response = target("/hello").queryParam("name", "Jersey").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("Hello Jersey!"));
    }

    @Test
    public void testResourceMethodWithOptionalIntGivenNoQueryParam() throws Exception {
        final Response response = target("/square").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("0"));
    }

    @Test
    public void testResourceMethodWithOptionalIntGivenQueryParam() throws Exception {
        final Response response = target("/square").queryParam("value", "42").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("1764"));
    }
}
