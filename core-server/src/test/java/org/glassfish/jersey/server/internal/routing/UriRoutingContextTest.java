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

package org.glassfish.jersey.server.internal.routing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.RequestContextBuilder;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class UriRoutingContextTest {

    public UriRoutingContextTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    private UriRoutingContext createContext(String requestUri, String method) {
        return new UriRoutingContext(RequestContextBuilder.from(requestUri, method).build());
    }

    private UriRoutingContext createContext(String appBaseUri, String requestUri, String method) {
        return new UriRoutingContext(RequestContextBuilder.from(appBaseUri, requestUri, method).build());
    }

    @Test
    public void testGetAbsolutePath() throws URISyntaxException {
        UriRoutingContext context;

        context = createContext("http://example.org/app/", "http://example.org/app/resource?foo1=bar1&foo2=bar2", "GET");
        assertEquals(URI.create("http://example.org/app/resource"), context.getAbsolutePath());

        context = createContext("http://example.org/app/", "http://example.org/app/resource%20decoded?foo1=bar1", "GET");
        assertEquals(URI.create("http://example.org/app/resource%20decoded"), context.getAbsolutePath());
    }

    @Test
    public void testGetPath() throws URISyntaxException {
        UriRoutingContext context;

        context = createContext("http://example.org/my%20app/resource?foo1=bar1&foo2=bar2", "GET");
        assertEquals("my app/resource", context.getPath());

        context = createContext("http://example.org/my%20app/", "http://example.org/my%20app/resource?foo1=bar1&foo2=bar2",
                "GET");
        assertEquals("resource", context.getPath());

        context = createContext("http://example.org/my%20app/",
                "http://example.org/my%20app/resource?foo1=bar1&foo2=bar2", "GET");
        assertEquals("resource", context.getPath());
    }

    @Test
    public void testGetDecodedPath() throws URISyntaxException {
        UriRoutingContext ctx = createContext("http://example.org/my%20app/resource?foo1=bar1&foo2=bar2", "GET");
        assertEquals("my%20app/resource", ctx.getPath(false));
        assertEquals("my app/resource", ctx.getPath(true));
    }

    @Test
    public void testGetPathBuilder() throws URISyntaxException {
        UriRoutingContext ctx = createContext("http://example.org/my%20app/",
                "http://example.org/my%20app/resource?foo1=bar1&foo2=bar2", "GET");
        assertEquals(URI.create("http://example.org/my%20app/resource"), ctx.getAbsolutePathBuilder().build());
    }

    @Test
    public void testGetPathSegments() throws URISyntaxException {
        List<PathSegment> lps = createContext("http://example.org/app/",
                "http://example.org/app/my%20resource/my%20subresource", "GET").getPathSegments();
        assertEquals(2, lps.size());
        assertEquals("my resource", lps.get(0).getPath());
        assertEquals("my subresource", lps.get(1).getPath());

        try {
            lps.remove(0);
            fail("UnsupportedOperationException expected - returned list should not be modifiable.");
        } catch (UnsupportedOperationException ex) {
            // passed
        }
    }

    @Test
    public void testGetPathSegments2() throws URISyntaxException {
        List<PathSegment> lps = createContext("http://example.org/app/",
                "http://example.org/app/my%20resource/my%20subresource", "GET").getPathSegments(false);
        assertEquals(2, lps.size());
        assertEquals("my%20resource", lps.get(0).getPath());
        assertEquals("my%20subresource", lps.get(1).getPath());

        try {
            lps.remove(0);
            fail("UnsupportedOperationException expected - returned list should not be modifiable.");
        } catch (UnsupportedOperationException ex) {
            // passed
        }
    }

    @Test
    public void testQueryParams() throws URISyntaxException {
        MultivaluedMap<String, String> map =
                createContext("http://example.org/app/resource?foo1=bar1&foo2=bar2", "GET").getQueryParameters();
        assertEquals(2, map.size());
        assertEquals("bar1", map.getFirst("foo1"));
        assertEquals("bar2", map.getFirst("foo2"));

        try {
            map.remove("foo1");
            fail("UnsupportedOperationException expected - returned list should not be modifiable.");
        } catch (UnsupportedOperationException ex) {
            // passed
        }
    }

    @Test
    public void testQueryParamsDecoded() throws URISyntaxException {
        MultivaluedMap<String, String> map =
                createContext("http://example.org/app/resource?foo1=%7Bbar1%7D&foo2=%7Bbar2%7D", "GET").getQueryParameters(true);
        assertEquals(2, map.size());
        assertEquals("{bar1}", map.getFirst("foo1"));
        assertEquals("{bar2}", map.getFirst("foo2"));
        try {
            map.remove("foo1");
            fail("UnsupportedOperationException expected - returned list should not be modifiable.");
        } catch (UnsupportedOperationException ex) {
            // passed
        }
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.PathSegmentsHttpRequestTest}.
     */
    @Test
    public void testGetPathSegmentsGeneral() {
        final UriInfo ui = createContext("/p1;x=1;y=1/p2;x=2;y=2/p3;x=3;y=3", "GET");

        List<PathSegment> segments = ui.getPathSegments();
        assertEquals(3, segments.size());

        final Iterator<PathSegment> psi = segments.iterator();
        PathSegment segment;

        segment = psi.next();
        assertEquals("p1", segment.getPath());
        MultivaluedMap<String, String> m = segment.getMatrixParameters();
        assertEquals("1", m.getFirst("x"));
        assertEquals("1", m.getFirst("y"));

        segment = psi.next();
        assertEquals("p2", segment.getPath());
        m = segment.getMatrixParameters();
        assertEquals("2", m.getFirst("x"));
        assertEquals("2", m.getFirst("y"));

        segment = psi.next();
        assertEquals("p3", segment.getPath());
        m = segment.getMatrixParameters();
        assertEquals("3", m.getFirst("x"));
        assertEquals("3", m.getFirst("y"));
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.PathSegmentsHttpRequestTest}.
     */
    @Test
    public void testGetPathSegmentsMultipleSlash() {
        final UriInfo ui = createContext("/p//p//p//", "GET");
        List<PathSegment> segments = ui.getPathSegments();
        assertEquals(7, segments.size());

        final Iterator<PathSegment> psi = segments.iterator();
        PathSegment segment;

        segment = psi.next();
        assertEquals("p", segment.getPath());
        assertEquals(0, segment.getMatrixParameters().size());

        segment = psi.next();
        assertEquals("", segment.getPath());
        assertEquals(0, segment.getMatrixParameters().size());

        segment = psi.next();
        assertEquals("p", segment.getPath());
        assertEquals(0, segment.getMatrixParameters().size());

        segment = psi.next();
        assertEquals("", segment.getPath());
        assertEquals(0, segment.getMatrixParameters().size());

        segment = psi.next();
        assertEquals("p", segment.getPath());
        assertEquals(0, segment.getMatrixParameters().size());

        segment = psi.next();
        assertEquals("", segment.getPath());
        assertEquals(0, segment.getMatrixParameters().size());

        segment = psi.next();
        assertEquals("", segment.getPath());
        assertEquals(0, segment.getMatrixParameters().size());
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.PathSegmentsHttpRequestTest}.
     */
    @Test
    public void testGetPathSegmentsMultipleMatrix() {
        final UriInfo ui = createContext("/p;x=1;x=2;x=3", "GET");
        List<PathSegment> segments = ui.getPathSegments();
        assertEquals(1, segments.size());

        final Iterator<PathSegment> psi = segments.iterator();
        PathSegment segment;

        segment = psi.next();
        MultivaluedMap<String, String> m = segment.getMatrixParameters();
        List<String> values = m.get("x");
        for (int i = 0; i < m.size(); i++) {
            assertEquals(Integer.valueOf(i + 1).toString(), values.get(i));
        }
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.PathSegmentsHttpRequestTest}.
     */
    @Test
    public void testGetPathSegmentsMultipleSlashmulitpleMatrix() {
        final UriInfo ui = createContext("/;x=1;y=1/;x=2;y=2/;x=3;y=3", "GET");
        List<PathSegment> segments = ui.getPathSegments();
        assertEquals(3, segments.size());

        final Iterator<PathSegment> psi = segments.iterator();
        PathSegment segment;

        segment = psi.next();
        MultivaluedMap<String, String> m = segment.getMatrixParameters();
        assertEquals("1", m.getFirst("x"));
        assertEquals("1", m.getFirst("y"));

        segment = psi.next();
        m = segment.getMatrixParameters();
        assertEquals("2", m.getFirst("x"));
        assertEquals("2", m.getFirst("y"));

        segment = psi.next();
        m = segment.getMatrixParameters();
        assertEquals("3", m.getFirst("x"));
        assertEquals("3", m.getFirst("y"));
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersGeneral() throws Exception {
        final UriInfo ui = createContext("/widgets/10?verbose=true&item=1&item=2", "GET");
        MultivaluedMap<String, String> p = ui.getQueryParameters();
        assertEquals(p.get("verbose").size(), 1);
        assertEquals(p.getFirst("verbose"), "true");
        assertEquals(p.get("item").size(), 2);
        assertEquals(p.getFirst("item"), "1");
        assertEquals(p.get("foo"), null);
        assertEquals(p.getFirst("foo"), null);
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersEmpty() throws Exception {
        final UriInfo ui = createContext("/widgets/10", "GET");
        MultivaluedMap<String, String> p = ui.getQueryParameters();
        assertEquals(p.size(), 0);
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersSingleAmpersand() throws Exception {
        final UriInfo ui = createContext("/widgets/10?&", "GET");
        MultivaluedMap<String, String> p = ui.getQueryParameters();
        assertEquals(p.size(), 0);
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersMultipleAmpersand() throws Exception {
        final UriInfo ui = createContext("/widgets/10?&&%20=%20&&&", "GET");
        MultivaluedMap<String, String> p = ui.getQueryParameters();
        assertEquals(p.size(), 1);
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersInterspersedAmpersand() throws Exception {
        final UriInfo ui = createContext("/widgets/10?a=1&&b=2", "GET");
        MultivaluedMap<String, String> p = ui.getQueryParameters();
        assertEquals(p.size(), 2);
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersEmptyValues() throws Exception {
        final UriInfo ui = createContext("/widgets/10?one&two&three", "GET");
        MultivaluedMap<String, String> p = ui.getQueryParameters();

        assertEquals(p.getFirst("one"), "");
        assertEquals(p.getFirst("two"), "");
        assertEquals(p.getFirst("three"), "");
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersMultipleEmptyValues() throws Exception {
        final UriInfo ui = createContext("/widgets/10?one&one&one", "GET");
        MultivaluedMap<String, String> p = ui.getQueryParameters();

        assertEquals(p.get("one").size(), 3);
        assertEquals(p.get("one").get(0), "");
        assertEquals(p.get("one").get(1), "");
        assertEquals(p.get("one").get(2), "");
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersWhiteSpace() throws Exception {
        final UriInfo ui = createContext("/widgets/10?x+=+1%20&%20y+=+2", "GET");
        MultivaluedMap<String, String> p = ui.getQueryParameters();

        assertEquals(" 1 ", p.getFirst("x "));
        assertEquals(" 2", p.getFirst(" y "));
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersDecoded() throws Exception {
        UriInfo ui;
        MultivaluedMap<String, String> p;

        ui = createContext("/widgets/10?x+=+1%20&%20y+=+2", "GET");
        p = ui.getQueryParameters();
        assertEquals(" 1 ", p.getFirst("x "));
        assertEquals(" 2", p.getFirst(" y "));

        ui = createContext("/widgets/10?x=1&y=1+%2B+2", "GET");
        p = ui.getQueryParameters(true);
        assertEquals("1", p.getFirst("x"));
        assertEquals("1 + 2", p.getFirst("y"));

        ui = createContext("/widgets/10?x=1&y=1+%26+2", "GET");
        p = ui.getQueryParameters(true);
        assertEquals("1", p.getFirst("x"));
        assertEquals("1 & 2", p.getFirst("y"));

        ui = createContext("/widgets/10?x=1&y=1+%7C%7C+2", "GET");
        p = ui.getQueryParameters(true);
        assertEquals("1", p.getFirst("x"));
        assertEquals("1 || 2", p.getFirst("y"));
    }

    /**
     * Migrated Jersey 1.x {@code com.sun.jersey.impl.QueryParametersHttpRequestTest}.
     */
    @Test
    public void testGetQueryParametersEncoded() throws Exception {
        final UriInfo ui = createContext("/widgets/10?x+=+1%20&%20y+=+2", "GET");
        MultivaluedMap<String, String> p = ui.getQueryParameters(false);

        assertEquals("+1%20", p.getFirst("x "));
        assertEquals("+2", p.getFirst(" y "));
    }
}
