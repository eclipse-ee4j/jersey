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

package org.glassfish.jersey.tests.integration.jersey2637;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Reproducer tests for JERSEY-2637 - Query params cannot be injected using {@link javax.ws.rs.FormParam}.
 */
public class Jersey2637DisabledITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new Jersey2637();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testFormParams() throws Exception {
        final Form form = new Form()
                .param("username", "user")
                .param("password", "pass");

        final Response response = target("disabled").request().post(Entity.form(form));

        assertThat(response.readEntity(String.class), is("user_pass"));
    }

    @Test
    public void testQueryParams() throws Exception {
        final Response response = target("disabled")
                .queryParam("username", "user").queryParam("password", "pass")
                .request()
                .post(Entity.form(new Form()));

        assertThat(response.readEntity(String.class), is("ko_ko"));
    }

    @Test
    public void testDoubleQueryParams() throws Exception {
        final Response response = target("disabled")
                .queryParam("username", "user").queryParam("password", "pass")
                .queryParam("username", "user").queryParam("password", "pass")
                .request()
                .post(Entity.form(new Form()));

        assertThat(response.readEntity(String.class), is("ko_ko"));
    }

    @Test
    public void testEncodedQueryParams() throws Exception {
        final Response response = target("disabled")
                .queryParam("username", "us%20er").queryParam("password", "pass")
                .request()
                .post(Entity.form(new Form()));

        assertThat(response.readEntity(String.class), is("ko_ko"));
    }

    @Test
    public void testFormAndQueryParams() throws Exception {
        final Form form = new Form()
                .param("username", "user")
                .param("password", "pass");

        final Response response = target("disabled")
                .queryParam("username", "user").queryParam("password", "pass")
                .request()
                .post(Entity.form(form));

        assertThat(response.readEntity(String.class), is("user_pass"));
    }

    @Test
    public void testFormAndDoubleQueryParams() throws Exception {
        final Form form = new Form()
                .param("username", "user")
                .param("password", "pass");

        final Response response = target("disabled")
                .queryParam("username", "user").queryParam("password", "pass")
                .queryParam("username", "user").queryParam("password", "pass")
                .request()
                .post(Entity.form(form));

        assertThat(response.readEntity(String.class), is("user_pass"));
    }
}
