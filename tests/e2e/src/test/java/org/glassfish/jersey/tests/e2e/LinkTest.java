/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.uri.UriTemplate;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Martin Matula
 */
public class LinkTest extends JerseyTest {

    @Path("resource")
    public static class Resource {

        @POST
        @Produces({
                MediaType.APPLICATION_XHTML_XML,
                MediaType.APPLICATION_ATOM_XML,
                MediaType.APPLICATION_SVG_XML
        })
        @Path("producesxml")
        public String producesXml() {
            return "";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class, LinkTestResource.class);
    }

    @Test
    public void testEquals() {
        Link link = Link.fromMethod(Resource.class, "producesXml").build();
        String string = link.toString();
        Link fromValueOf = Link.valueOf(string);
        assertEquals(link, fromValueOf);
    }

    @Test
    public void testFromResourceMethod() {
        Link link = Link.fromMethod(Resource.class, "producesXml").build();
        assertEquals("producesxml", link.getUri().toString());
    }

    @Test
    public void testDelimiters() {
        Link.Builder builder = Link.fromUri("http://localhost:80");
        final String value = "param1value1    param1value2";
        builder = builder.param("param1", value);
        Link link = builder.build();
        final Map<String, String> params = link.getParams();
        assertEquals(value, params.get("param1"));
    }

    @Path("linktest")
    public static class LinkTestResource {

        @GET
        public Response get(@Context UriInfo uriInfo) throws Exception {
            URI test1 = URI.create(uriInfo.getAbsolutePath().toString() + "test1");
            URI test2 = URI.create(uriInfo.getAbsolutePath().toString() + "test2");

            return Response.ok()
                    .link("http://oracle.com", "parent")
                    .link(new URI("http://jersey.java.net"), "framework")
                    .links(
                            Link.fromUri(uriInfo.relativize(test1)).rel("test1").build(),
                            Link.fromUri(test2).rel("test2").build(),
                            Link.fromUri(uriInfo.relativize(URI.create("linktest/test3"))).rel("test3").build()
                    ).build();
        }
    }

    @Test
    public void simpleLinkTest() {
        final WebTarget target = target("/linktest/");
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
