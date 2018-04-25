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

import javax.ws.rs.core.Link;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.tests.e2e.common.TestRuntimeDelegate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests for LinkProvider class.
 *
 * @author Santiago Pericas-Geertsen (Santiago.PericasGeertsen at oracle.com)
 */
public class LinkProviderTest {

    @Before
    public void setUp() throws Exception {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    @After
    public void tearDown() throws Exception {
        RuntimeDelegate.setInstance(null);
    }

    @Test
    public void testValueOf() {
        final Link l1 = Link.fromUri("http://example.org/app/link1").build();
        Link l2 = Link.valueOf("<http://example.org/app/link1>");
        assertEquals(l1, l2);
        l2 = Link.valueOf(" <http://example.org/app/link1>");
        assertEquals(l1, l2);
        l2 = Link.valueOf(" <http://example.org/app/link1> ");
        assertEquals(l1, l2);
    }

    @Test
    public void testValueOfExceptions() {
        int nOfExceptions = 0;
        try {
            Link.valueOf("http://example.org/app/link1>");
        } catch (final IllegalArgumentException e) {
            nOfExceptions++;
        }
        try {
            Link.valueOf("<http://example.org/app/link1");
        } catch (final IllegalArgumentException e) {
            nOfExceptions++;
        }
        try {
            Link.valueOf("http://example.org/app/link1");
        } catch (final IllegalArgumentException e) {
            nOfExceptions++;
        }
        assertEquals(nOfExceptions, 3);
    }

    @Test
    public void testValueOfParams() {
        final Link l1 = Link.fromUri("http://example.org/app/link1").param("foo1", "bar1").param("foo2", "bar2").build();
        Link l2 = Link.valueOf("<http://example.org/app/link1>; foo1=\"bar1\"; foo2 = \"bar2\"");
        assertEquals(l1, l2);
        l2 = Link.valueOf("<http://example.org/app/link1>; foo2=\"bar2\"; foo1= \"bar1\"");
        assertEquals(l1, l2);
        l2 = Link.valueOf("<http://example.org/app/link1>;      foo1=\"bar1\";     foo2=\"bar2\"");
        assertEquals(l1, l2);
        l2 = Link.valueOf("< http://example.org/app/link1   >;      foo1=\"bar1\";     foo2=\"bar2\"");
        assertEquals(l1, l2);
    }

    @Test
    public void testRoundTrip() {
        final Link l1 = Link.fromUri("http://example.org/app/link1").param("foo1", "bar1").param("foo2", "bar2").build();
        assertEquals(l1, Link.valueOf(l1.toString()));
        final Link l2 = Link.valueOf("<http://example.org/app/link1>; foo1=\"bar1\"; foo2 = \"bar2\"");
        assertEquals(l1, l2);
    }

    @Test
    public void testWithoutDoubleQuotes() {
        final Link l1 = Link.fromUri("http://example.org/app/link1").param("foo1", "bar1").param("foo2", "bar2").build();
        assertEquals(l1, Link.valueOf(l1.toString()));
        final Link l2 = Link.valueOf("<http://example.org/app/link1>; foo1=bar1; foo2 = bar2");
        assertEquals(l1, l2);
    }
}
