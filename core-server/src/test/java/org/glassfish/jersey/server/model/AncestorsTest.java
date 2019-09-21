/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.RequestContextBuilder;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Taken from Jersey-1: jersey-tests:com.sun.jersey.impl.subresources.AncestorsTest
 *
 * @author Paul Sandoz
 */
public class AncestorsTest {

    ApplicationHandler app;

    private ApplicationHandler createApplication(Class<?>... classes) {
        return new ApplicationHandler(new ResourceConfig(classes));
    }

    @Path("/node")
    public static class Node {

        int n = 0;

        public Node() {
        }

        private Node(int i) {
            this.n = i;
        }

        @Path("node")
        public Node getChild() {
            return new Node(n + 1);
        }

        @GET
        public String get(@Context UriInfo ui) {
            assertEquals(n + 1, ui.getMatchedResources().size());

            for (int i = 0; i <= n; i++) {
                Node node = (Node) ui.getMatchedResources().get(i);
                assertEquals(n - i, node.n);
            }

            assertEquals(n + 1, ui.getMatchedURIs().size());
            for (int i = 0; i <= n; i++) {
                String p = ui.getMatchedURIs().get(i);
                assertEquals(getPath(n - i), p);
            }

            return Integer.toString(n);
        }

        @Path("leaf")
        @GET
        public String getSub(@Context UriInfo ui) {
            assertEquals(n + 1, ui.getMatchedResources().size());

            for (int i = 0; i <= n; i++) {
                Node node = (Node) ui.getMatchedResources().get(i);
                assertEquals(n - i, node.n);
            }

            assertEquals(n + 1 + 1, ui.getMatchedURIs().size());
            String p = ui.getMatchedURIs().get(0);
            assertEquals(getPathLeaf(n), p);
            for (int i = 0; i <= n; i++) {
                p = ui.getMatchedURIs().get(i + 1);
                assertEquals(getPath(n - i), p);
            }

            return Integer.toString(n);
        }

        protected String getPath(int n) {
            String p = "node";
            for (int i = 1; i <= n; i++) {
                p += "/node";
            }
            return p;
        }

        protected String getPathLeaf(int n) {
            return getPath(n) + "/leaf";
        }
    }

    @Test
    public void testNode() throws Exception {
        app = createApplication(Node.class);

        assertEquals("0", app.apply(RequestContextBuilder.from("/node", "GET").build()).get().getEntity());
        assertEquals("1", app.apply(RequestContextBuilder.from("/node/node", "GET").build()).get().getEntity());
        assertEquals("2", app.apply(RequestContextBuilder.from("/node/node/node", "GET").build()).get().getEntity());
        assertEquals("3", app.apply(RequestContextBuilder.from("/node/node/node/node", "GET").build()).get().getEntity());
    }

    @Test
    public void testNodeLeaf() throws Exception {
        app = createApplication(Node.class);

        assertEquals("0", app.apply(RequestContextBuilder.from("/node/leaf", "GET").build()).get().getEntity());
        assertEquals("1", app.apply(RequestContextBuilder.from("/node/node/leaf", "GET").build()).get().getEntity());
        assertEquals("2", app.apply(RequestContextBuilder.from("/node/node/node/leaf", "GET").build()).get().getEntity());
        assertEquals("3", app.apply(RequestContextBuilder.from("/node/node/node/node/leaf", "GET").build()).get().getEntity());
    }

    @Path("/node/")
    public static class NodeSlash {

        int n = 0;

        public NodeSlash() {
        }

        private NodeSlash(int i) {
            this.n = i;
        }

        @Path("node/")
        public NodeSlash getChild() {
            return new NodeSlash(n + 1);
        }

        @GET
        public String get(@Context UriInfo ui) {
            assertEquals(n + 1, ui.getMatchedResources().size());
            for (int i = 0; i <= n; i++) {
                NodeSlash node = (NodeSlash) ui.getMatchedResources().get(i);
                assertEquals(n - i, node.n);
            }

            assertEquals(n + 1, ui.getMatchedURIs().size());
            for (int i = 0; i <= n; i++) {
                String p = ui.getMatchedURIs().get(i);
                assertEquals(getPath(n - i), p);
            }

            return Integer.toString(n);
        }

        @Path("leaf/")
        @GET
        public String getSub(@Context UriInfo ui) {
            assertEquals(n + 1, ui.getMatchedResources().size());

            for (int i = 0; i <= n; i++) {
                NodeSlash node = (NodeSlash) ui.getMatchedResources().get(i);
                assertEquals(n - i, node.n);
            }

            assertEquals(n + 1 + 1, ui.getMatchedURIs().size());
            String p = ui.getMatchedURIs().get(0);
            assertEquals(getPathLeaf(n), p);
            for (int i = 0; i <= n; i++) {
                p = ui.getMatchedURIs().get(i + 1);
                assertEquals(getPath(n - i), p);
            }

            return Integer.toString(n);
        }

        protected String getPath(int n) {
            String p = "node/";
            for (int i = 1; i <= n; i++) {
                p += "node/";
            }
            return p.substring(0, p.length() - 1);
        }

        protected String getPathLeaf(int n) {
            return getPath(n) + "/leaf";
        }
    }

    @Test
    public void testNodeSlash() throws Exception {
        app = createApplication(NodeSlash.class);

        assertEquals("0", app.apply(RequestContextBuilder.from("/node/", "GET").build()).get().getEntity());
        assertEquals("1", app.apply(RequestContextBuilder.from("/node/node/", "GET").build()).get().getEntity());
        assertEquals("2", app.apply(RequestContextBuilder.from("/node/node/node/", "GET").build()).get().getEntity());
        assertEquals("3", app.apply(RequestContextBuilder.from("/node/node/node/node/", "GET").build()).get().getEntity());
    }

    @Test
    public void testNodeLeafSlash() throws Exception {
        app = createApplication(NodeSlash.class);

        assertEquals("0", app.apply(RequestContextBuilder.from("/node/leaf/", "GET").build()).get().getEntity());
        assertEquals("1", app.apply(RequestContextBuilder.from("/node/node/leaf/", "GET").build()).get().getEntity());
        assertEquals("2", app.apply(RequestContextBuilder.from("/node/node/node/leaf/", "GET").build()).get().getEntity());
        assertEquals("3", app.apply(RequestContextBuilder.from("/node/node/node/node/leaf/", "GET").build()).get().getEntity());
    }

    @Path("foo")
    public static class FooResource {

        @Context
        UriInfo ui;

        @GET
        public String getFoo() {
            assertEquals(1, ui.getMatchedResources().size());
            assertEquals(this, ui.getMatchedResources().get(0));
            assertEquals(1, ui.getMatchedURIs().size());
            assertEquals("foo", ui.getMatchedURIs().get(0));
            return "foo";
        }

        @Path("bar")
        public BarResource getBarResource() {
            assertEquals(1, ui.getMatchedResources().size());
            assertEquals(this, ui.getMatchedResources().get(0));
            assertEquals(2, ui.getMatchedURIs().size());
            assertEquals("foo/bar", ui.getMatchedURIs().get(0));
            assertEquals("foo", ui.getMatchedURIs().get(1));
            return new BarResource(ui, this);
        }
    }

    public static class BarResource {

        UriInfo ui;
        FooResource f;

        BarResource(UriInfo ui, FooResource f) {
            this.ui = ui;
            this.f = f;
        }

        @GET
        public String getBar() {
            assertEquals(2, ui.getMatchedResources().size());
            assertEquals(this, ui.getMatchedResources().get(0));
            assertEquals(f, ui.getMatchedResources().get(1));
            assertEquals(2, ui.getMatchedURIs().size());
            assertEquals("foo/bar", ui.getMatchedURIs().get(0));
            assertEquals("foo", ui.getMatchedURIs().get(1));
            return "bar";
        }
    }

    @Test
    public void testFooBar() throws Exception {
        app = createApplication(FooResource.class);

        assertEquals("foo", app.apply(RequestContextBuilder.from("/foo", "GET").build()).get().getEntity());
        assertEquals("bar", app.apply(RequestContextBuilder.from("/foo/bar", "GET").build()).get().getEntity());
    }
}
