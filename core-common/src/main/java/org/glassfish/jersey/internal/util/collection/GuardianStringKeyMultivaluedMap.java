/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The {@link MultivaluedMap} wrapper that is able to set guards observing changes of values represented by a key.
 * @param <V> The value type of the wrapped {@code MultivaluedMap}.
 *
 * @since 2.38
 */
public class GuardianStringKeyMultivaluedMap<V> implements MultivaluedMap<String, V> {

    private final MultivaluedMap<String, V> inner;
    private final Map<String, Boolean> guards = new HashMap<>();

    private static boolean isMutable(Object mutable) {
        return !String.class.isInstance(mutable) && !MediaType.class.isInstance(mutable);
    }

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
        V first = inner.getFirst(key);
        if (isMutable(key)) {
            observe(key);
        }
        return first;
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
        final List<V> innerList = inner.get(key);
        if (innerList != null) {
            for (Map.Entry<String, Boolean> guard : guards.entrySet()) {
                if (guard.getKey().equals(key)) {
                    return new GuardianList(innerList, guard);
                }
            }
        }
        return innerList;
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
        observeAll();
        return inner.values();
    }

    @Override
    public Set<Entry<String, List<V>>> entrySet() {
        observeAll();
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
        if (observed != null) {
            guards.put(key, false);
        }
        return observed != null && observed;
    }

    private void observe(String key) {
        for (Map.Entry<String, Boolean> guard : guards.entrySet()) {
            if (guard.getKey().equals(key)) {
                guard.setValue(true);
                break;
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

    private static class MutableGuardian<V> {
        protected final Map.Entry<String, Boolean> guard;

        private MutableGuardian(Entry<String, Boolean> guard) {
            this.guard = guard;
        }

        protected V guardMutable(V mutable) {
            if (isMutable(mutable)) {
                guard.setValue(true);
            }
            return mutable;
        }
    }

    private static class GuardianList<V> extends MutableGuardian<V> implements List<V>  {
        private final List<V> guarded;

        public GuardianList(List<V> guarded, Map.Entry<String, Boolean> guard) {
            super(guard);
            this.guarded = guarded;
        }

        @Override
        public int size() {
            return guarded.size();
        }

        @Override
        public boolean isEmpty() {
            return guarded.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return guarded.contains(o);
        }

        @Override
        public Iterator<V> iterator() {
            return new GuardianIterator<>(guarded.iterator(), guard);
        }

        @Override
        public Object[] toArray() {
            guard.setValue(true);
            return guarded.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            guard.setValue(true);
            return guarded.toArray(a);
        }

        @Override
        public boolean add(V e) {
            guard.setValue(true);
            return guarded.add(e);
        }

        @Override
        public boolean remove(Object o) {
            guard.setValue(true);
            return guarded.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return guarded.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            guard.setValue(true);
            return guarded.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends V> c) {
            guard.setValue(true);
            return guarded.addAll(index, c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            guard.setValue(true);
            return guarded.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            guard.setValue(true);
            return guarded.retainAll(c);
        }

        @Override
        public void clear() {
            guard.setValue(true);
            guarded.clear();
        }

        @Override
        public V get(int index) {
            return guardMutable(guarded.get(index));
        }

        @Override
        public V set(int index, V element) {
            guard.setValue(true);
            return guarded.set(index, element);
        }

        @Override
        public void add(int index, V element) {
            guard.setValue(true);
            guarded.add(index, element);
        }

        @Override
        public V remove(int index) {
            guard.setValue(true);
            return guarded.remove(index);
        }

        @Override
        public int indexOf(Object o) {
            return guarded.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return guarded.lastIndexOf(o);
        }

        @Override
        public ListIterator<V> listIterator() {
            return new GuardianListIterator<>(guarded.listIterator(), guard);
        }

        @Override
        public ListIterator<V> listIterator(int index) {
            return new GuardianListIterator<>(guarded.listIterator(index), guard);
        }

        @Override
        public List<V> subList(int fromIndex, int toIndex) {
            final List<V> sublist = guarded.subList(fromIndex, toIndex);
            return sublist != null ? new GuardianList<>(sublist, guard) : sublist;
        }

        @Override
        public String toString() {
            return guarded.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (GuardianList.class.isInstance(obj)) {
                return guarded.equals(((GuardianList) obj).guarded);
            }
            return guarded.equals(obj);
        }

        @Override
        public int hashCode() {
            return guarded.hashCode();
        }
    }

    private static class GuardianIterator<V> extends MutableGuardian<V> implements Iterator<V> {
        protected final Iterator<V> guarded;

        public GuardianIterator(Iterator<V> guarded, Map.Entry<String, Boolean> guard) {
            super(guard);
            this.guarded = guarded;
        }

        @Override
        public boolean hasNext() {
            return guarded.hasNext();
        }

        @Override
        public V next() {
            return guardMutable(guarded.next());
        }

        @Override
        public void remove() {
            guard.setValue(true);
            guarded.remove();
        }

        @Override
        public void forEachRemaining(Consumer<? super V> action) {
            guarded.forEachRemaining(action);
        }

        @Override
        public String toString() {
            return guarded.toString();
        }
    }

    private static class GuardianListIterator<V> extends GuardianIterator<V> implements ListIterator<V> {

        public GuardianListIterator(Iterator<V> guarded, Entry<String, Boolean> guard) {
            super(guarded, guard);
        }

        @Override
        public boolean hasPrevious() {
            return ((ListIterator<V>) guarded).hasPrevious();
        }

        @Override
        public V previous() {
            return guardMutable(((ListIterator<V>) guarded).previous());
        }

        @Override
        public int nextIndex() {
            return ((ListIterator<V>) guarded).nextIndex();
        }

        @Override
        public int previousIndex() {
            return ((ListIterator<V>) guarded).previousIndex();
        }

        @Override
        public void set(V v) {
            ((ListIterator<V>) guarded).set(v);
            guard.setValue(true);
        }

        @Override
        public void add(V v) {
            ((ListIterator<V>) guarded).add(v);
            guard.setValue(true);
        }
    }
}
