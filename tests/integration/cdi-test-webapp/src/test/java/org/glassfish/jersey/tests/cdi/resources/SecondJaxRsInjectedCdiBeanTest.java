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

package org.glassfish.jersey.tests.cdi.resources;

import java.net.URI;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.test.JerseyTest;

import org.jboss.weld.environment.se.Weld;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test that a raw CDI managed bean gets JAX-RS injected.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class SecondJaxRsInjectedCdiBeanTest extends JerseyTest {
    Weld weld;

    @Override
    public void setUp() throws Exception {
        weld = new Weld();
        weld.initialize();
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        weld.shutdown();
        super.tearDown();
    }

    @Override
    protected Application configure() {
        return new SecondaryApplication();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("cdi-test-webapp/secondary").build();
    }

    @Test
    public void testPathAndHeader() {
        _testPathAndHeader(target());
    }

    public static void _testPathAndHeader(final WebTarget webTarget) {
        final WebTarget target = webTarget.path("non-jaxrs-bean-injected");

        final Response pathResponse = target.path("path/2").request().get();
        assertThat(pathResponse.getStatus(), is(200));
        final String path = pathResponse.readEntity(String.class);

        assertThat(path, is("non-jaxrs-bean-injected/path/2"));

        final Response headerResponse = target.path("header/2").request().header("x-test", "bummer2").get();
        assertThat(headerResponse.getStatus(), is(200));
        final String header = headerResponse.readEntity(String.class);

        assertThat(header, is("bummer2"));
    }
}
