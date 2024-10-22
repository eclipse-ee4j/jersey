/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.RuntimeType;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Miroslav Fuksa
 *
 */
public class PropertiesHelperTest {

    @Test
    public void testGetValueWithType() {
        Map<String, String> properties = new HashMap<>();
        final String key = "my.property";
        properties.put(key, "15");

        assertEquals("15", PropertiesHelper.getValue(properties, key, String.class, null));
        assertEquals(Integer.valueOf(15), PropertiesHelper.getValue(properties, key, Integer.class, null));
        assertEquals(Long.valueOf(15), PropertiesHelper.getValue(properties, key, Long.class, null));
    }

    @Test
    public void testGetValueWithTypeAndDefaultValue() {
        Map<String, String> properties = new HashMap<>();
        final String key = "my.property";
        properties.put(key, "15");

        assertEquals("15", PropertiesHelper.getValue(properties, key, "80", String.class, null));
        assertEquals(Integer.valueOf(30), PropertiesHelper.getValue(properties, "non.existing", 30, Integer.class, null));
        assertEquals(Long.valueOf(20), PropertiesHelper.getValue(properties, "non.existing", 20L, Long.class, null));
    }

    @Test
    public void testGetValueWithDefaultValue() {
        Map<String, String> properties = new HashMap<>();
        final String key = "my.property";
        properties.put(key, "15");

        assertEquals("15", PropertiesHelper.getValue(properties, key, "80", null));
        assertEquals(Integer.valueOf(30), PropertiesHelper.getValue(properties, "non.existing", 30, null));
        assertEquals(Long.valueOf(20), PropertiesHelper.getValue(properties, "non.existing", 20L, null));
    }

    @Test
    public void testGetValueByIgnoredRuntime() {
        Map<String, String> properties = new HashMap<>();
        final String key = "my.property";
        properties.put(key, "15");

        assertEquals("15", PropertiesHelper.getValue(properties, RuntimeType.CLIENT, key, String.class, null));
        assertEquals(Integer.valueOf(15), PropertiesHelper.getValue(properties, RuntimeType.CLIENT, key, Integer.class,
                null));
        assertEquals(Long.valueOf(15), PropertiesHelper.getValue(properties, RuntimeType.SERVER, key, Long.class, null));
    }

    @Test
    public void testGetValueByRuntime1() {
        Map<String, String> properties = new HashMap<>();
        final String key = "jersey.config.my.property";
        properties.put(key, "15");
        properties.put("jersey.config.client.my.property", "999");
        properties.put("jersey.config.server.my.property", "1");

        assertEquals("999", PropertiesHelper.getValue(properties, RuntimeType.CLIENT, key, String.class, null));
        assertEquals(Integer.valueOf(999), PropertiesHelper.getValue(properties, RuntimeType.CLIENT, key, Integer.class,
                null));
        assertEquals(Long.valueOf(1), PropertiesHelper.getValue(properties, RuntimeType.SERVER, key, Long.class, null));

        assertEquals("15", PropertiesHelper.getValue(properties, key, String.class, null));
        assertEquals(Integer.valueOf(15), PropertiesHelper.getValue(properties, key, Integer.class, null));
        assertEquals(Long.valueOf(15), PropertiesHelper.getValue(properties, key, Long.class, null));
    }

    @Test
    public void testGetValueByRuntime2() {
        Map<String, String> properties = new HashMap<>();
        final String key = "jersey.config.my.property";
        properties.put(key, "15");
        properties.put("jersey.config.client.my.property", "999");

        assertEquals("999", PropertiesHelper.getValue(properties, RuntimeType.CLIENT, key, String.class, null));
        assertEquals(Integer.valueOf(999), PropertiesHelper.getValue(properties, RuntimeType.CLIENT, key, Integer.class,
                null));
        assertEquals(Long.valueOf(15), PropertiesHelper.getValue(properties, RuntimeType.SERVER, key, Long.class, null));
        assertEquals(Long.valueOf(15), PropertiesHelper.getValue(properties, RuntimeType.SERVER, key, 800L, Long.class,
                null));

        assertEquals("15", PropertiesHelper.getValue(properties, key, String.class, null));
        assertEquals(Integer.valueOf(15), PropertiesHelper.getValue(properties, key, Integer.class, null));
        assertEquals(Long.valueOf(15), PropertiesHelper.getValue(properties, key, Long.class, null));
    }

    @Test
    public void testGetValueByRuntime3() {
        Map<String, String> properties = new HashMap<>();
        final String key = "jersey.config.my.property";
        properties.put("jersey.config.client.my.property", "999");

        assertEquals("999", PropertiesHelper.getValue(properties, RuntimeType.CLIENT, key, String.class, null));
        assertNull(PropertiesHelper.getValue(properties, key, String.class, null));
        assertNull(PropertiesHelper.getValue(properties, RuntimeType.SERVER, key, String.class, null));
        assertEquals("55", PropertiesHelper.getValue(properties, key, "55", String.class, null));
    }

    /**
     * There is a value but of different type and no way how to transform.
     */
    @Test
    public void testGetValueNoTransformation() {
        Map<String, Object> properties = new HashMap<>();
        final String key = "my.property";
        properties.put(key, Boolean.TRUE);

        assertNull(PropertiesHelper.getValue(properties, key, Integer.class, null));
        //look at System.out, there is a message:
        //      WARNING: There is no way how to transform value "true" [java.lang.Boolean] to type [java.lang.Integer].
    }

    @Test
    public void testFallback() {
        Map<String, String> fallback = new HashMap<>();
        fallback.put("my.property", "my.old.property");

        Map<String, Object> properties = new HashMap<>();
        properties.put("my.old.property", "foo");

        assertEquals("foo", PropertiesHelper.getValue(properties, "my.property", String.class, fallback));
    }

    @Test
    public void testPropertyNameDeclination() {
        String myProperty = "jersey.config.my.property";
        String myClientProperty = "jersey.config.client.my.property";
        String myServerProperty = "jersey.config.server.my.property";
        String myNonJerseyProperty = "my.property";

        assertEquals(myProperty, PropertiesHelper.getPropertyNameForRuntime(myProperty, null));

        assertEquals(myClientProperty, PropertiesHelper.getPropertyNameForRuntime(myProperty, RuntimeType.CLIENT));
        assertEquals(myServerProperty, PropertiesHelper.getPropertyNameForRuntime(myProperty, RuntimeType.SERVER));

        assertEquals(myServerProperty, PropertiesHelper.getPropertyNameForRuntime(myServerProperty, RuntimeType.SERVER));
        assertEquals(myServerProperty, PropertiesHelper.getPropertyNameForRuntime(myServerProperty, RuntimeType.CLIENT));

        assertEquals(myClientProperty, PropertiesHelper.getPropertyNameForRuntime(myClientProperty, RuntimeType.SERVER));
        assertEquals(myClientProperty, PropertiesHelper.getPropertyNameForRuntime(myClientProperty, RuntimeType.CLIENT));

        assertEquals(myNonJerseyProperty, PropertiesHelper.getPropertyNameForRuntime(myNonJerseyProperty, RuntimeType.CLIENT));
        assertEquals(myNonJerseyProperty, PropertiesHelper.getPropertyNameForRuntime(myNonJerseyProperty, RuntimeType.CLIENT));
        assertEquals(myNonJerseyProperty, PropertiesHelper.getPropertyNameForRuntime(myNonJerseyProperty, null));
    }

    @Test
    public void testconvertValue() {
        Long lg = 10L;
        long l = 10L;
        Integer ig = 10;
        int i = 10;

        assertEquals(ig, PropertiesHelper.convertValue(i, Integer.class));
        assertEquals(ig, PropertiesHelper.convertValue(ig, Integer.class));
        assertEquals(ig, PropertiesHelper.convertValue(l, Integer.class));
        assertEquals(ig, PropertiesHelper.convertValue(lg, Integer.class));
        assertEquals(lg, PropertiesHelper.convertValue(i, Long.class));
        assertEquals(lg, PropertiesHelper.convertValue(ig, Long.class));
        assertEquals(lg, PropertiesHelper.convertValue(l, Long.class));
        assertEquals(lg, PropertiesHelper.convertValue(lg, Long.class));
        assertEquals(ig, PropertiesHelper.convertValue(i, int.class));
        assertEquals(ig, PropertiesHelper.convertValue(ig, int.class));
        assertEquals(ig, PropertiesHelper.convertValue(l, int.class));
        assertEquals(ig, PropertiesHelper.convertValue(lg, int.class));
        assertEquals(lg, PropertiesHelper.convertValue(i, long.class));
        assertEquals(lg, PropertiesHelper.convertValue(ig, long.class));
        assertEquals(lg, PropertiesHelper.convertValue(l, long.class));
        assertEquals(lg, PropertiesHelper.convertValue(lg, long.class));

    }

    @Test
    public void isPropertyOrNotSetTest() {
        assertEquals(false, PropertiesHelper.isPropertyOrNotSet((Boolean) null));
        assertEquals(true, PropertiesHelper.isPropertyOrNotSet(Boolean.TRUE));
        assertEquals(false, PropertiesHelper.isPropertyOrNotSet(Boolean.FALSE));
        assertEquals(false, PropertiesHelper.isPropertyOrNotSet((String) null));
        assertEquals(true, PropertiesHelper.isPropertyOrNotSet(""));
        assertEquals(false, PropertiesHelper.isPropertyOrNotSet("treu")); // false for non-boolean values
        assertEquals(true, PropertiesHelper.isPropertyOrNotSet("TRUE"));
        assertEquals(false, PropertiesHelper.isPropertyOrNotSet("false"));
    }
}
