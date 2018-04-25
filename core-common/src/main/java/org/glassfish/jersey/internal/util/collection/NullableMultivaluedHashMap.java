/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * An implementation of {@link MultivaluedMap} where values can be {@code null}.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values which can contain {@code null} values
 *
 * @author Petr Bouda
 */
public class NullableMultivaluedHashMap<K, V> extends MultivaluedHashMap<K, V> {

    public NullableMultivaluedHashMap() {
        super();
    }

    public NullableMultivaluedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public NullableMultivaluedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public NullableMultivaluedHashMap(MultivaluedMap<? extends K, ? extends V> map) {
        super(map);
    }

    @Override
    protected void addFirstNull(final List<V> values) {
        values.add(null);
    }

    @Override
    protected void addNull(final List<V> values) {
        values.add(null);
    }
}
