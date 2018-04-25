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

package org.glassfish.jersey.server.internal.inject;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Utility class that maps the primitive types to their respective classes as well
 * as the default values as defined by the JAX-RS specification.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class PrimitiveMapper {

    static final Map<Class, Class> primitiveToClassMap =
            getPrimitiveToClassMap();
    static final Map<Class, Object> primitiveToDefaultValueMap =
            getPrimitiveToDefaultValueMap();

    private static Map<Class, Class> getPrimitiveToClassMap() {
        Map<Class, Class> m = new WeakHashMap<>();
        // Put all primitive to wrapper class mappings except
        // that for Character
        m.put(Boolean.TYPE, Boolean.class);
        m.put(Byte.TYPE, Byte.class);
        m.put(Character.TYPE, Character.class);
        m.put(Short.TYPE, Short.class);
        m.put(Integer.TYPE, Integer.class);
        m.put(Long.TYPE, Long.class);
        m.put(Float.TYPE, Float.class);
        m.put(Double.TYPE, Double.class);

        return Collections.unmodifiableMap(m);
    }

    private static Map<Class, Object> getPrimitiveToDefaultValueMap() {
        Map<Class, Object> m = new WeakHashMap<>();
        m.put(Boolean.class, Boolean.valueOf(false));
        m.put(Byte.class, Byte.valueOf((byte) 0));
        m.put(Character.class, Character.valueOf((char) 0x00));
        m.put(Short.class, Short.valueOf((short) 0));
        m.put(Integer.class, Integer.valueOf(0));
        m.put(Long.class, Long.valueOf(0L));
        m.put(Float.class, Float.valueOf(0.0f));
        m.put(Double.class, Double.valueOf(0.0d));

        return Collections.unmodifiableMap(m);
    }

    /**
     * Prevents instantiation.
     */
    private PrimitiveMapper() {
    }
}
