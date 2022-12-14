/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The {@link MultivaluedMap} wrapper that is able to set guards observing changes of values represented by a key.
 * @param <V> The value type of the wrapped {@code MultivaluedMap}.
 *
 * @since 2.38
 */
public class GuardianStringKeyMultivaluedMap<V> implements MultivaluedMap<String, V> {

    private final MultivaluedMap<String, V> inner;
    private final Map<String, Boolean> guards = new HashMap<>();

    public GuardianStringKeyMultivaluedMap(MultivaluedMap<String, V> inner) {
        this.inner = inner;
    }

    @Override
    public void putSingle(String key, V value) {
        observe(key);
        inner.putSingle(key, value);
    }

    @Override
    public void add(String key, V value) {
        observe(key);
        inner.add(key, value);
    }

    @Override
    public V getFirst(String key) {
        return inner.getFirst(key);
    }

    @Override
    public void addAll(String key, V... newValues) {
        observe(key);
        inner.addAll(key, newValues);
    }

    @Override
    public void addAll(String key, List<V> valueList) {
        observe(key);
        inner.addAll(key, valueList);
    }

    @Override
    public void addFirst(String key, V value) {
        observe(key);
        inner.addFirst(key, value);
    }

    @Override
    public boolean equalsIgnoreValueOrder(MultivaluedMap<String, V> otherMap) {
        return inner.equalsIgnoreValueOrder(otherMap);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return inner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return inner.containsValue(value);
    }

    @Override
    public List<V> get(Object key) {
        return inner.get(key);
    }

    @Override
    public List<V> put(String key, List<V> value) {
        observe(key);
        return inner.put(key, value);
    }

    @Override
    public List<V> remove(Object key) {
        if (key != null) {
            observe(key.toString());
        }
        return inner.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<V>> m) {
        for (String key : m.keySet()) {
            observe(key);
        }
        inner.putAll(m);
    }

    @Override
    public void clear() {
        observeAll();
        inner.clear();
    }

    @Override
    public Set<String> keySet() {
        return inner.keySet();
    }

    @Override
    public Collection<List<V>> values() {
        return inner.values();
    }

    @Override
    public Set<Entry<String, List<V>>> entrySet() {
        return inner.entrySet();
    }

    /**
     * Observe changes of a value represented by the key.
     * @param key the key values to observe
     */
    public void setGuard(String key) {
        guards.put(key, false);
    }

    /**
     * Get all the guarded keys
     * @return a {@link Set} of keys guarded.
     */
    public Set<String> getGuards() {
        return guards.keySet();
    }

    /**
     * Return true when the value represented by the key has changed. Resets any observation - the operation is not idempotent.
     * @param key the Key observed.
     * @return whether the value represented by the key has changed.
     */
    public boolean isObservedAndReset(String key) {
        Boolean observed = guards.get(key);
        guards.put(key, false);
        return observed != null && observed;
    }

    private void observe(String key) {
        for (Map.Entry<String, Boolean> guard : guards.entrySet()) {
            if (guard.getKey().equals(key)) {
                guard.setValue(true);
            }
        }
    }

    private void observeAll() {
        for (Map.Entry<String, Boolean> guard : guards.entrySet()) {
            guard.setValue(true);
        }
    }

    @Override
    public String toString() {
        return inner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuardianStringKeyMultivaluedMap<?> that = (GuardianStringKeyMultivaluedMap<?>) o;
        return inner.equals(that.inner) && guards.equals(that.guards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inner, guards);
    }
}
