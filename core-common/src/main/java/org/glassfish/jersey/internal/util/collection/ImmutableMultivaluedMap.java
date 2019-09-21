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

package org.glassfish.jersey.internal.util.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * An immutable view of a {@link MultivaluedMap}.
 *
 * @param <K> the key
 * @param <V> the value
 * @author Gili Tzabari
 * @author Michal Gajdos
 */
public class ImmutableMultivaluedMap<K, V> implements MultivaluedMap<K, V> {

    /**
     * Returns an empty immutable map.
     *
     * @return an empty immutable map.
     */
    public static <K, V> ImmutableMultivaluedMap<K, V> empty() {
        return new ImmutableMultivaluedMap<K, V>(new MultivaluedHashMap<K, V>());
    }

    private final MultivaluedMap<K, V> delegate;

    /**
     * Creates a new ImmutableMultivaluedMap.
     *
     * @param delegate the underlying MultivaluedMap
     */
    public ImmutableMultivaluedMap(final MultivaluedMap<K, V> delegate) {
        if (delegate == null) {
            throw new NullPointerException("ImmutableMultivaluedMap delegate must not be 'null'.");
        }
        this.delegate = delegate;
    }

    @Override
    public boolean equalsIgnoreValueOrder(final MultivaluedMap<K, V> otherMap) {
        return delegate.equalsIgnoreValueOrder(otherMap);
    }

    @Override
    public void putSingle(final K key, final V value) {
        throw new UnsupportedOperationException("This MultivaluedMap implementation is immutable.");
    }

    @Override
    public void add(final K key, final V value) {
        throw new UnsupportedOperationException("This MultivaluedMap implementation is immutable.");
    }

    @Override
    public V getFirst(final K key) {
        return delegate.getFirst(key);
    }

    @Override
    public void addAll(final K key, final V... newValues) {
        throw new UnsupportedOperationException("This MultivaluedMap implementation is immutable.");
    }

    @Override
    public void addAll(final K key, final List<V> valueList) {
        throw new UnsupportedOperationException("This MultivaluedMap implementation is immutable.");
    }

    @Override
    public void addFirst(final K key, final V value) {
        throw new UnsupportedOperationException("This MultivaluedMap implementation is immutable.");
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public List<V> get(final Object key) {
        return delegate.get(key);
    }

    @Override
    public List<V> put(final K key, final List<V> value) {
        throw new UnsupportedOperationException("This MultivaluedMap implementation is immutable.");
    }

    @Override
    public List<V> remove(final Object key) {
        throw new UnsupportedOperationException("This MultivaluedMap implementation is immutable.");
    }

    @Override
    public void putAll(final Map<? extends K, ? extends List<V>> m) {
        throw new UnsupportedOperationException("This MultivaluedMap implementation is immutable.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This MultivaluedMap implementation is immutable.");
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(delegate.keySet());
    }

    @Override
    public Collection<List<V>> values() {
        return Collections.unmodifiableCollection(delegate.values());
    }

    @Override
    public Set<Entry<K, List<V>>> entrySet() {
        return Collections.unmodifiableSet(delegate.entrySet());
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableMultivaluedMap)) {
            return false;
        }

        final ImmutableMultivaluedMap that = (ImmutableMultivaluedMap) o;

        if (!delegate.equals(that.delegate)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
