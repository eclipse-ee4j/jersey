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

import org.glassfish.jersey.message.internal.ParameterizedHeader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link ParameterizedHeadersMap}.
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class ParameterizedHeadersMapTest {

    private ParameterizedHeadersMap map;

    @Before
    public void setUp() throws Exception {
        map = new ParameterizedHeadersMap();
    }

    @After
    public void tearDown() throws Exception {
        map = null;
    }

    /**
     * Test of add method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testAdd() throws Exception {
        map.add("foo", new ParameterizedHeader("bar"));
        List<ParameterizedHeader> values = map.get("foo");

        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals("bar", values.get(0).getValue());

        map.add("foo", new ParameterizedHeader("baz"));

        assertEquals(1, map.size());

        values = map.get("foo");

        assertEquals(2, values.size());
        assertEquals("bar", values.get(0).getValue());
        assertEquals("baz", values.get(1).getValue());

        map.add("bop", new ParameterizedHeader("boo"));
        assertEquals(2, map.size());
    }

    /**
     * Test of clear method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testClear() throws Exception {
        map.add("foo", new ParameterizedHeader("bar"));
        map.add("baz", new ParameterizedHeader("bop"));

        assertTrue(!map.isEmpty());
        assertEquals(2, map.size());

        map.clear();

        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    /**
     * Test of containsKey method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testContainsKey() throws Exception {
        map.add("foo", new ParameterizedHeader("bar"));

        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("FOO"));
        assertTrue(map.containsKey("Foo"));
        assertTrue(map.containsKey("fOo"));
        assertTrue(map.containsKey("foO"));
        assertTrue(!map.containsKey("bar"));
    }

    /**
     * Test of containsValue method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testContainsValue() throws Exception {
        List<ParameterizedHeader> values = new ArrayList<>();
        values.add(new ParameterizedHeader("bar"));
        values.add(new ParameterizedHeader("bop"));

        map.put("foo", values);
        assertTrue(map.containsValue(values));

        map.clear();
        assertTrue(!map.containsValue(values));
    }

    /**
     * Test of entrySet method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testEntrySet() throws Exception {
        List<ParameterizedHeader> valuesFoo = new ArrayList<>();
        valuesFoo.add(new ParameterizedHeader("foo1"));
        valuesFoo.add(new ParameterizedHeader("foo2"));

        map.put("foo", valuesFoo);

        List<ParameterizedHeader> valuesBar = new ArrayList<>();
        valuesBar.add(new ParameterizedHeader("bar1"));
        valuesBar.add(new ParameterizedHeader("bar2"));

        map.put("bar", valuesBar);

        Set<Entry<String, List<ParameterizedHeader>>> entrySet = map.entrySet();

        assertEquals(2, entrySet.size());

        // TODO - detailed tests for the HeadersEntries methods
    }

    /**
     * Test of get method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testGet() throws Exception {
        map.add("foo", new ParameterizedHeader("bar"));

        assertNotNull(map.get("foo"));
        assertNotNull(map.get("FOO"));
        assertNotNull(map.get("Foo"));
        assertNotNull(map.get("fOo"));
        assertNotNull(map.get("foO"));
        assertNull(map.get("bar"));

        List<ParameterizedHeader> values = map.get("foo");

        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals("bar", values.get(0).getValue());
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        ParameterizedHeadersMap map2 = new ParameterizedHeadersMap();

        List<ParameterizedHeader> valuesFoo = new ArrayList<>();
        valuesFoo.add(new ParameterizedHeader("foo1"));
        valuesFoo.add(new ParameterizedHeader("foo2"));

        map.put("foo", valuesFoo);
        map2.put("foo", valuesFoo);

        List<ParameterizedHeader> valuesBar = new ArrayList<>();
        valuesBar.add(new ParameterizedHeader("bar1"));
        valuesBar.add(new ParameterizedHeader("bar2"));

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
     * Test of getFirst method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testGetFirst() throws Exception {
        map.add("foo", new ParameterizedHeader("bar"));
        map.add("foo", new ParameterizedHeader("baz"));
        map.add("foo", new ParameterizedHeader("bop"));

        assertEquals(3, map.get("foo").size());
        assertEquals("bar", map.getFirst("foo").getValue());
    }

    /**
     * Test of isEmpty method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testIsEmpty() throws Exception {
        assertTrue(map.isEmpty());

        map.add("foo", new ParameterizedHeader("bar"));
        assertTrue(!map.isEmpty());

        map.clear();
        assertTrue(map.isEmpty());
    }

    /**
     * Test of keySet method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testKeySet() throws Exception {
        map.add("foo", new ParameterizedHeader("bar"));
        map.add("baz", new ParameterizedHeader("bop"));

        Set<String> keySet = map.keySet();

        assertNotNull(keySet);
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("foo"));
        assertTrue(!keySet.contains("bar"));
        assertTrue(keySet.contains("baz"));
        assertTrue(!keySet.contains("bop"));

        // TODO - detailed tests for the HeadersKeys methods
    }

    @Test
    public void testParameters() throws Exception {
        ParameterizedHeader header;
        header = new ParameterizedHeader("foo");

        assertEquals("foo", header.getValue());
        assertEquals(0, header.getParameters().size());

        header = new ParameterizedHeader("foo;bar=baz;bop=abc");

        assertEquals("foo", header.getValue());
        assertEquals(2, header.getParameters().size());
        assertEquals("baz", header.getParameters().get("bar"));
        assertEquals("abc", header.getParameters().get("bop"));
    }

    /**
     * Test of put method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testPut() throws Exception {
        List<ParameterizedHeader> fooValues1 = new ArrayList<>();
        fooValues1.add(new ParameterizedHeader("foo1"));
        fooValues1.add(new ParameterizedHeader("foo2"));

        assertNull(map.get("foo"));

        map.put("foo", fooValues1);

        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsValue(fooValues1));
        assertTrue(map.get("foo") == fooValues1);

        List<ParameterizedHeader> fooValues2 = new ArrayList<>();
        fooValues2.add(new ParameterizedHeader("foo3"));
        fooValues2.add(new ParameterizedHeader("foo4"));

        map.put("foo", fooValues2);

        assertEquals(1, map.size());
        assertTrue(map.containsKey("foo"));
        assertTrue(!map.containsValue(fooValues1));
        assertTrue(map.containsValue(fooValues2));
        assertTrue(map.get("foo") == fooValues2);
    }

    /**
     * Test of putAll method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testPutAll() throws Exception {
        Map<String, List<ParameterizedHeader>> all = new HashMap<>();

        List<ParameterizedHeader> fooValues = new ArrayList<>();
        fooValues.add(new ParameterizedHeader("foo1"));
        fooValues.add(new ParameterizedHeader("foo2"));

        all.put("foo", fooValues);

        List<ParameterizedHeader> barValues = new ArrayList<>();
        barValues.add(new ParameterizedHeader("bar1"));
        barValues.add(new ParameterizedHeader("bar2"));

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
     * Test of putSingle method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testPutSingle() throws Exception {
        List<ParameterizedHeader> values = new ArrayList<>();
        values.add(new ParameterizedHeader("bar"));
        values.add(new ParameterizedHeader("baz"));

        map.put("foo", values);

        assertEquals(1, map.size());
        assertEquals(2, map.get("foo").size());

        map.putSingle("foo", new ParameterizedHeader("bop"));

        assertEquals(1, map.size());
        assertEquals(1, map.get("foo").size());
        assertEquals("bop", map.get("foo").get(0).getValue());
    }

    /**
     * Test of remove method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testRemove() throws Exception {
        map.add("foo", new ParameterizedHeader("bar"));
        map.add("baz", new ParameterizedHeader("bop"));

        assertEquals(2, map.size());
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("baz"));

        map.remove("foo");

        assertEquals(1, map.size());
        assertTrue(!map.containsKey("foo"));
        assertTrue(map.containsKey("baz"));
    }

    /**
     * Test of size method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testSize() throws Exception {
        assertEquals(0, map.size());

        map.add("foo", new ParameterizedHeader("bar"));
        assertEquals(1, map.size());

        map.add("foo", new ParameterizedHeader("arg"));
        assertEquals(1, map.size());

        map.add("baz", new ParameterizedHeader("bop"));
        assertEquals(2, map.size());

        map.remove("baz");
        assertEquals(1, map.size());

        map.remove("foo");
        assertEquals(0, map.size());
    }

    /**
     * Test of values method, of class ParametrizedHeadersMap.
     */
    @Test
    public void testValues() throws Exception {
        Map<String, List<ParameterizedHeader>> all = new HashMap<>();

        List<ParameterizedHeader> fooValues = new ArrayList<>();
        fooValues.add(new ParameterizedHeader("foo1"));
        fooValues.add(new ParameterizedHeader("foo2"));

        all.put("foo", fooValues);

        List<ParameterizedHeader> barValues = new ArrayList<>();
        barValues.add(new ParameterizedHeader("bar1"));
        barValues.add(new ParameterizedHeader("bar2"));

        all.put("bar", barValues);

        map.putAll(all);

        Collection<List<ParameterizedHeader>> values = map.values();

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
