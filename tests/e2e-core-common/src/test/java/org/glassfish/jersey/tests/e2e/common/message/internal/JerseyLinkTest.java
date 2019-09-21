/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.message.internal;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Link;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.tests.e2e.common.TestRuntimeDelegate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for LinkTest class.
 *
 * @author Santiago Pericas-Geertsen (Santiago.PericasGeertsen at oracle.com)
 */
public class JerseyLinkTest {

    @Path("/myresource")
    static class MyResource {

        @GET
        @Produces("text/plain")
        public String self() {
            return "myself";
        }

        @GET
        @Produces("application/xml")
        public String notSelf() {
            return "<xml>notSelf</xml>";
        }
    }

    @Before
    public void setUp() throws Exception {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    @After
    public void tearDown() throws Exception {
        RuntimeDelegate.setInstance(null);
    }

    @Test
    public void testGetUri() {
        URI u = URI.create("http://example.org/app/link1");
        Link l1 = Link.fromUri("http://example.org/app/link1").param("foo1", "bar1").param("foo2", "bar2").build();
        assertEquals(l1.getUri(), u);
        assertEquals(l1.getUriBuilder().build(), u);
    }

    @Test
    public void testToString() {
        Link link = Link.fromUri("http://example.org/app/link1").rel("self").build();
        assertEquals("<http://example.org/app/link1>; rel=\"self\"", link.toString());
    }

    @Test
    public void testGetters() {
        Link link = Link.fromUri("http://example.org/app/link1").rel("self").type("text/plain").build();
        assertEquals(URI.create("http://example.org/app/link1"), link.getUri());
        assertEquals("self", link.getRel());
        assertEquals(null, link.getTitle());
        assertEquals("text/plain", link.getType());
        assertEquals(2, link.getParams().size());
    }

    /**
     * Regression test for JERSEY-1378 fix.
     */
    @Test
    public void buildRelativeLinkTest() {
        assertEquals(URI.create("aa%20bb"), Link.fromUri("aa bb").build().getUri());
    }

    /**
     * Reproducer for JERSEY-2387: IAE expected on unresolved URI template parameters.
     */
    @Test
    public void testLinkBuilderWithUnresolvedTemplates() {
        Link.Builder linkBuilder;
        try {
            linkBuilder = Link.fromUri("scheme://authority/{x1}/{x2}/{x3}");
            linkBuilder.build("p");
            fail("IllegalArgumentException is expected to be thrown from Link.Builder when there are unresolved templates.");
        } catch (IllegalArgumentException expected) {
            // exception expected, move on...
        }
        try {
            linkBuilder = Link.fromUri("scheme://authority/{x1}/{x2}/{x3}");
            linkBuilder.build();
            fail("IllegalArgumentException is expected to be thrown from Link.Builder when there are unresolved templates.");
        } catch (IllegalArgumentException expected) {
            // exception expected, move on...
        }
    }
}
