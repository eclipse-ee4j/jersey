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

package org.glassfish.jersey.tests.integration.servlet_25_init_1;

import java.net.URI;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.uri.UriTemplate;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Servlet 2.5 initialization test #01.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Martin Matula
 */
public class Servlet25Init1ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new Servlet25init1();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testHelloWorld() throws Exception {
        String s = target().path("servlet_path/helloworld").request().get(String.class);
        assertEquals("Hello World! " + this.getClass().getPackage().getName(), s);
    }

    @Test
    public void testHelloWorldAtWrongPath() {
        Response r = target().path("application_path/helloworld").request().get();
        assertTrue(
                "Request to application_path/helloworld should have failed, but did not. That means two applications are "
                        + "registered.",
                r.getStatus() >= 400);
    }

    @Test
    public void testHelloWorldViaClientInResource() throws Exception {
        String s = target().path("servlet_path/viaclient/helloworld").request().get(String.class);
        assertEquals("Hello World! " + this.getClass().getPackage().getName(), s);
    }

    @Test
    public void testUnreachableResource() {
        Response r = target().path("servlet_path/unreachable").request().get();
        assertTrue("Managed to reach a resource that is not registered in the application.", r.getStatus() >= 400);
    }

    @Test
    public void testUnreachableResourceAtWrongPath() {
        Response r = target().path("application_path/unreachable").request().get();
        assertTrue("Managed to reach a resource that is not registered in the application.", r.getStatus() >= 400);
    }

    @Test
    public void testInjection() {
        String s = target().path("servlet_path/helloworld/injection").request().get(String.class);
        assertEquals("GETtruetestServlet1testServlet1/", s);
    }

    // Reproducer for JERSEY-1801
    @Test
    public void multipleLinksTest() {
        final WebTarget target = target("/servlet_path/links/");
        final Response response = target.request().get();
        assertThat(response.getStatus(), equalTo(200));

        final URI targetUri = target.getUri();
        assertThat(response.getLink("parent").getUri(), equalTo(URI.create("http://oracle.com")));
        assertThat(response.getLink("framework").getUri(), equalTo(URI.create("http://jersey.java.net")));

        assertThat(response.getLink("test1").getUri(), equalTo(UriTemplate.resolve(targetUri, "test1")));
        assertThat(response.getLink("test2").getUri(), equalTo(UriTemplate.resolve(targetUri, "test2")));
        assertThat(response.getLink("test3").getUri(), equalTo(UriTemplate.resolve(targetUri, "test3")));
    }

}
