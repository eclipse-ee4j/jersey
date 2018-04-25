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

package org.glassfish.jersey.internal.util.collection;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * An implementation of {@link MultivaluedMap} where keys and values are
 * instances of String.
 * <p />
 * This map has an additional ability to instantiate classes using the
 * individual string values as a constructor parameters.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class MultivaluedStringMap extends MultivaluedHashMap<String, String> {

    static final long serialVersionUID = -6052320403766368902L;

    public MultivaluedStringMap(MultivaluedMap<? extends String, ? extends String> map) {
        super(map);
    }

    public MultivaluedStringMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public MultivaluedStringMap(int initialCapacity) {
        super(initialCapacity);
    }

    public MultivaluedStringMap() {
        super();
    }

    @Override
    protected void addFirstNull(List<String> values) {
        values.add("");
    }

    @Override
    protected void addNull(List<String> values) {
        values.add(0, "");
    }

    public final <A> A getFirst(String key, Class<A> type) {
        String value = getFirst(key);
        if (value == null) {
            return null;
        }
        Constructor<A> c = null;
        try {
            c = type.getConstructor(String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException(type.getName() + " has no String constructor", ex);
        }
        A retVal = null;
        try {
            retVal = c.newInstance(value);
        } catch (Exception ex) {
        }
        return retVal;
    }

    @SuppressWarnings("unchecked")
    public final <A> A getFirst(String key, A defaultValue) {
        String value = getFirst(key);
        if (value == null) {
            return defaultValue;
        }

        Class<A> type = (Class<A>) defaultValue.getClass();

        Constructor<A> c = null;
        try {
            c = type.getConstructor(String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException(type.getName() + " has no String constructor", ex);
        }
        A retVal = defaultValue;
        try {
            retVal = c.newInstance(value);
        } catch (Exception ex) {
        }
        return retVal;
    }
}
