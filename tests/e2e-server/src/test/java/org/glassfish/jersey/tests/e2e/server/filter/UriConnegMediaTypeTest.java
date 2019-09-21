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

package org.glassfish.jersey.tests.e2e.server.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 * @author Martin Matula
 */
public class UriConnegMediaTypeTest extends JerseyTest {

    @Override
    protected Application configure() {
        Map<String, MediaType> mediaTypes = new HashMap<>();
        mediaTypes.put("foo", MediaType.valueOf("application/foo"));
        mediaTypes.put("bar", MediaType.valueOf("application/bar"));
        mediaTypes.put("foot", MediaType.valueOf("application/foot"));

        Set<Class<?>> classes = new HashSet<>();

        for (Class<?> c : UriConnegMediaTypeTest.class.getClasses()) {
            if (c.getAnnotation(Path.class) != null) {
                classes.add(c);
            }
        }

        ResourceConfig rc = new ResourceConfig(classes);
        rc.property(ServerProperties.MEDIA_TYPE_MAPPINGS, mediaTypes);
        return rc;
    }

    public abstract static class Base {
        @GET
        @Produces("application/foo")
        public String doGetFoo(@Context HttpHeaders headers) {
            assertEquals(1, headers.getAcceptableMediaTypes().size());
            return "foo";
        }

        @GET
        @Produces("application/foot")
        public String doGetFoot() {
            return "foot";
        }

        @GET
        @Produces("application/bar")
        public String doGetBar() {
            return "bar";
        }
    }

    @Path("/abc")
    public static class SingleSegment extends Base {
    }

    @Path("/xyz/")
    public static class SingleSegmentSlash extends Base {
    }

    @Path("/xyz/abc")
    public static class MultipleSegments extends Base {
    }

    @Path("/xyz/xxx/")
    public static class MultipleSegmentsSlash extends Base {
    }

    @Path("/xyz/abc.xml")
    public static class DotPrefixSegments extends Base {
    }

    @Path("/foo_bar_foot")
    public static class PathWithSuffixSegment extends Base {
    }

    @Path("/")
    public static class SubResourceMethods extends Base {
        @Path("sub")
        @GET
        @Produces("application/foo")
        public String doGetFooS() {
            return "foo";
        }

        @Path("sub")
        @GET
        @Produces("application/foot")
        public String doGetFootS() {
            return "foot";
        }

        @Path("sub")
        @GET
        @Produces("application/bar")
        public String doGetBarS() {
            return "bar";
        }
    }

    @Test
    public void testSlash() throws IOException {
        _test("/");
    }

    @Test
    public void testSingleSegment() throws IOException {
        _test("/", "abc");
    }

    @Test
    public void testSingleSegmentSlash() throws IOException {
        _test("/", "xyz", "/");
    }

    @Test
    public void testMultipleSegments() throws IOException {
        _test("/xyz", "abc");
    }

    @Test
    public void testMultipleSegmentsSlash() throws IOException {
        _test("/xyz", "xxx", "/");
    }

    @Test
    public void testDotPrefixSegments() throws IOException {
        _test("/xyz", "abc.xml");
        _test("/xyz", "abc", ".xml");
    }

    @Test
    public void testXXXSegment() throws IOException {
        _test("/", "foo_bar_foot");
    }

    @Test
    public void testSubResourceMethods() throws IOException {
        _test("/", "sub");
    }

    private void _test(String base) {
        _test(base, "", "");
    }

    private void _test(String base, String path) {
        _test(base, path, "");
    }

    private void _test(String base, String path, String terminate) {
        WebTarget r = target().path(base);

        String s = r.path(path + ".foo" + terminate).request().get(String.class);
        assertEquals("foo", s);

        s = r.path(path + ".foo" + terminate).request("application/bar").get(String.class);
        assertEquals("foo", s);

        s = r.path(path + ".foot" + terminate).request().get(String.class);
        assertEquals("foot", s);

        s = r.path(path + ".bar" + terminate).request().get(String.class);
        assertEquals("bar", s);

        s = r.path(path + terminate).request("application/foo").get(String.class);
        assertEquals("foo", s);

        s = r.path(path + terminate).request("application/foot").get(String.class);
        assertEquals("foot", s);

        s = r.path(path + terminate).request("application/foo;q=0.1").get(String.class);
        assertEquals("foo", s);
    }
}
