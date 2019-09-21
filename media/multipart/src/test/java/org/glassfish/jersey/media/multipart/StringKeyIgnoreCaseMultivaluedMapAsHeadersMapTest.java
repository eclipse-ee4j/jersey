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

package org.glassfish.jersey.media.multipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link StringKeyIgnoreCaseMultivaluedMap}.
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class StringKeyIgnoreCaseMultivaluedMapAsHeadersMapTest {

    @Before
    public void setUp() throws Exception {
        map = new StringKeyIgnoreCaseMultivaluedMap();
    }

    @After
    public void tearDown() throws Exception {
        map = null;
    }

    private StringKeyIgnoreCaseMultivaluedMap map;

    /**
     * Test of add method, of class HeadersMap.
     */
    @Test
    public void testAdd() {
        map.add("foo", "bar");
        List values = map.get("foo");

        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals("bar", values.get(0));

        map.add("foo", "baz");

        assertEquals(1, map.size());
        values = map.get("foo");
        assertEquals(2, values.size());
        assertEquals("bar", values.get(0));
        assertEquals("baz", values.get(1));

        map.add("bop", "boo");

        assertEquals(2, map.size());
    }

    /**
     * Test of clear method, of class HeadersMap.
     */
    @Test
    public void testClear() {
        map.add("foo", "bar");
        map.add("baz", "bop");

        assertTrue(!map.isEmpty());
        assertEquals(2, map.size());

        map.clear();

        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    /**
     * Test of containsKey method, of class HeadersMap.
     */
    @Test
    public void testContainsKey() {
        map.add("foo", "bar");

        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("FOO"));
        assertTrue(map.containsKey("Foo"));
        assertTrue(map.containsKey("fOo"));
        assertTrue(map.containsKey("foO"));
        assertTrue(!map.containsKey("bar"));
    }

    /**
     * Test of containsValue method, of class HeadersMap.
     */
    @Test
    public void testContainsValue() {
        List values = new ArrayList();
        values.add("bar");
        values.add("bop");

        map.put("foo", values);
        assertTrue(map.containsValue(values));

        map.clear();
        assertTrue(!map.containsValue(values));
    }

    /**
     * Test of entrySet method, of class HeadersMap.
     */
    @Test
    public void testEntrySet() {
        List valuesFoo = new ArrayList();
        valuesFoo.add("foo1");
        valuesFoo.add("foo2");

        map.put("foo", valuesFoo);

        List valuesBar = new ArrayList();
        valuesBar.add("bar1");
        valuesBar.add("bar2");

        map.put("bar", valuesBar);

        Set<Entry<String, List<String>>> entrySet = map.entrySet();
        assertEquals(2, entrySet.size());

        // TODO - detailed tests for the HeadersEntries methods
    }

    @Test
    public void testEqualsAndHashCode() {
        StringKeyIgnoreCaseMultivaluedMap map2 = new StringKeyIgnoreCaseMultivaluedMap();

        List valuesFoo = new ArrayList();
        valuesFoo.add("foo1");
        valuesFoo.add("foo2");

        map.put("foo", valuesFoo);
        map2.put("foo", valuesFoo);

        List valuesBar = new ArrayList();
        valuesBar.add("bar1");
        valuesBar.add("bar2");

        map.put("bar", valuesBar);
        map2.put("bar", valuesBar);

        assertTrue(map.equals(map2));
        assertTrue(map2.equals(map));
        assertEquals(map.hashCode(), map2.hashCode());

        map2.remove("bar");

        assertTrue(!map.equals(map2));
        assertTrue(!map2.equals(map));
    }

    /**
     * Test of get method, of class HeadersMap.
     */
    @Test
    public void testGet() {
        map.add("foo", "bar");
        assertNotNull(map.get("foo"));
        assertNotNull(map.get("FOO"));
        assertNotNull(map.get("Foo"));
        assertNotNull(map.get("fOo"));
        assertNotNull(map.get("foO"));
        assertNull(map.get("bar"));

        List values = map.get("foo");
        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals("bar", values.get(0));
    }

    /**
     * Test of getFirst method, of class HeadersMap.
     */
    @Test
    public void testGetFirst() {
        map.add("foo", "bar");
        map.add("foo", "baz");
        map.add("foo", "bop");

        assertEquals(3, map.get("foo").size());
        assertEquals("bar", map.getFirst("foo"));
    }

    /**
     * Test of isEmpty method, of class HeadersMap.
     */
    @Test
    public void testIsEmpty() {
        assertTrue(map.isEmpty());

        map.add("foo", "bar");
        assertTrue(!map.isEmpty());

        map.clear();
        assertTrue(map.isEmpty());
    }

    /**
     * Test of keySet method, of class HeadersMap.
     */
    @Test
    public void testKeySet() {
        map.add("foo", "bar");
        map.add("baz", "bop");

        Set<String> keySet = map.keySet();

        assertNotNull(keySet);
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("foo"));
        assertTrue(!keySet.contains("bar"));
        assertTrue(keySet.contains("baz"));
        assertTrue(!keySet.contains("bop"));

        // TODO - detailed tests for the HeadersKeys methods
    }

    /**
     * Test of put method, of class HeadersMap.
     */
    @Test
    public void testPut() {
        List fooValues1 = new ArrayList();
        fooValues1.add("foo1");
        fooValues1.add("foo2");

        assertNull(map.get("foo"));

        map.put("foo", fooValues1);
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsValue(fooValues1));
        assertTrue(map.get("foo") == fooValues1);

        List fooValues2 = new ArrayList();
        fooValues2.add("foo3");
        fooValues2.add("foo4");
        map.put("foo", fooValues2);

        assertEquals(1, map.size());
        assertTrue(map.containsKey("foo"));
        assertTrue(!map.containsValue(fooValues1));
        assertTrue(map.containsValue(fooValues2));
        assertTrue(map.get("foo") == fooValues2);
    }

    /**
     * Test of putAll method, of class HeadersMap.
     */
    @Test
    public void testPutAll() {
        Map<String, List<String>> all = new HashMap<>();
        List fooValues = new ArrayList();
        fooValues.add("foo1");
        fooValues.add("foo2");
        all.put("foo", fooValues);
        List barValues = new ArrayList();
        barValues.add("bar1");
        barValues.add("bar2");
        all.put("bar", barValues);

        assertTrue(map.isEmpty());

        map.putAll(all);

        assertTrue(!map.isEmpty());
        assertEquals(2, map.size());
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("bar"));
        assertTrue(map.containsValue(fooValues));
        assertTrue(map.containsValue(barValues));
    }

    /**
     * Test of putSingle method, of class HeadersMap.
     */
    @Test
    public void testPutSingle() {
        List values = new ArrayList();
        values.add("bar");
        values.add("baz");
        map.put("foo", values);
        assertEquals(1, map.size());
        assertEquals(2, map.get("foo").size());
        map.putSingle("foo", "bop");
        assertEquals(1, map.size());
        assertEquals(1, map.get("foo").size());
        assertEquals("bop", map.get("foo").get(0));
    }

    /**
     * Test of remove method, of class HeadersMap.
     */
    @Test
    public void testRemove() {
        map.add("foo", "bar");
        map.add("baz", "bop");

        assertEquals(2, map.size());
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("baz"));

        map.remove("foo");

        assertEquals(1, map.size());
        assertTrue(!map.containsKey("foo"));
        assertTrue(map.containsKey("baz"));
    }

    /**
     * Test of size method, of class HeadersMap.
     */
    @Test
    public void testSize() {
        assertEquals(0, map.size());

        map.add("foo", "bar");
        assertEquals(1, map.size());

        map.add("foo", "arg");
        assertEquals(1, map.size());

        map.add("baz", "bop");
        assertEquals(2, map.size());

        map.remove("baz");
        assertEquals(1, map.size());

        map.remove("foo");
        assertEquals(0, map.size());
    }

    /**
     * Test of values method, of class HeadersMap.
     */
    @Test
    public void testValues() {
        Map<String, List<String>> all = new HashMap<>();

        List<String> fooValues = new ArrayList<>();
        fooValues.add("foo1");
        fooValues.add("foo2");
        all.put("foo", fooValues);

        List<String> barValues = new ArrayList<>();
        barValues.add("bar1");
        barValues.add("bar2");
        all.put("bar", barValues);

        map.putAll(all);
        Collection<List<String>> values = map.values();

        assertNotNull(values);
        assertEquals(2, values.size());

        List array[] = new List[2];
        array = values.toArray(array);

        if (array[0] == fooValues) {
            assertTrue(array[1] == barValues);
        } else if (array[0] == barValues) {
            assertTrue(array[1] == fooValues);
        } else {
            fail("Returned values were corrupted");
        }
    }

}
