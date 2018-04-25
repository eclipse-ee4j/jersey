/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.AbstractMap;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.glassfish.jersey.internal.guava.Preconditions.checkNotNull;

/**
 * Collections utils, which provide transforming views for {@link List} and {@link Map}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class Views {

    private Views() {
        // prevent instantiation.
    }

    /**
     * Create a {@link List} view, which transforms the values of provided original list.
     * <p>
     * Removing elements from the view is supported, adding and setting isn't and
     * throws {@link UnsupportedOperationException} when invoked.
     *
     * @param originalList original list.
     * @param transformer  transforming functions.
     * @param <T>          transformed type parameter.
     * @param <R>          type of the element from provided list.
     * @return transformed list view.
     */
    public static <T, R> List<T> listView(List<R> originalList, Function<R, T> transformer) {
        return new AbstractSequentialList<T>() {
            @Override
            public ListIterator<T> listIterator(int index) {
                return new ListIterator<T>() {

                    final ListIterator<R> iterator = originalList.listIterator(index);

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public T next() {
                        return transformer.apply(iterator.next());
                    }

                    @Override
                    public boolean hasPrevious() {
                        return iterator.hasPrevious();
                    }

                    @Override
                    public T previous() {
                        return transformer.apply(iterator.previous());
                    }

                    @Override
                    public int nextIndex() {
                        return iterator.nextIndex();
                    }

                    @Override
                    public int previousIndex() {
                        return iterator.previousIndex();
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }

                    @Override
                    public void set(T t) {
                        throw new UnsupportedOperationException("Not supported.");
                    }

                    @Override
                    public void add(T t) {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                };
            }

            @Override
            public int size() {
                return originalList.size();
            }
        };
    }

    /**
     * Create a {@link Map} view, which transforms the values of provided original map.
     * <p>
     * Removing elements from the map view is supported, adding and setting isn't and
     * throws {@link UnsupportedOperationException} when invoked.
     *
     * @param originalMap       provided map.
     * @param valuesTransformer values transformer.
     * @param <K>               key type.
     * @param <V>               transformed value type.
     * @param <O>               original value type.
     * @return transformed map view.
     */
    public static <K, V, O> Map<K, V> mapView(Map<K, O> originalMap, Function<O, V> valuesTransformer) {
        return new AbstractMap<K, V>() {
            @Override
            public Set<Entry<K, V>> entrySet() {
                return new AbstractSet<Entry<K, V>>() {


                    Set<Entry<K, O>> originalSet = originalMap.entrySet();
                    Iterator<Entry<K, O>> original = originalSet.iterator();

                    @Override
                    public Iterator<Entry<K, V>> iterator() {
                        return new Iterator<Entry<K, V>>() {
                            @Override
                            public boolean hasNext() {
                                return original.hasNext();
                            }

                            @Override
                            public Entry<K, V> next() {

                                Entry<K, O> next = original.next();

                                return new Entry<K, V>() {
                                    @Override
                                    public K getKey() {
                                        return next.getKey();
                                    }

                                    @Override
                                    public V getValue() {
                                        return valuesTransformer.apply(next.getValue());
                                    }

                                    @Override
                                    public V setValue(V value) {
                                        throw new UnsupportedOperationException("Not supported.");
                                    }
                                };
                            }

                            @Override
                            public void remove() {
                                original.remove();
                            }
                        };
                    }

                    @Override
                    public int size() {
                        return originalSet.size();
                    }
                };
            }
        };
    }

    /**
     * Create a view of an union of provided sets.
     * <p>
     * View is updated whenever any of the provided set changes.
     *
     * @param set1 first set.
     * @param set2 second set.
     * @param <E>  set item type.
     * @return union view of given sets.
     */
    public static <E> Set<E> setUnionView(final Set<? extends E> set1, final Set<? extends E> set2) {
        checkNotNull(set1, "set1");
        checkNotNull(set2, "set2");

        return new AbstractSet<E>() {
            @Override
            public Iterator<E> iterator() {
                return getUnion(set1, set2).iterator();
            }

            @Override
            public int size() {
                return getUnion(set1, set2).size();
            }

            private Set<E> getUnion(Set<? extends E> set1, Set<? extends E> set2) {
                HashSet<E> hashSet = new HashSet<>(set1);
                hashSet.addAll(set2);
                return hashSet;
            }
        };
    }

    /**
     * Create a view of a difference of provided sets.
     * <p>
     * View is updated whenever any of the provided set changes.
     *
     * @param set1 first set.
     * @param set2 second set.
     * @param <E>  set item type.
     * @return union view of given sets.
     */
    public static <E> Set<E> setDiffView(final Set<? extends E> set1, final Set<? extends E> set2) {
        checkNotNull(set1, "set1");
        checkNotNull(set2, "set2");

        return new AbstractSet<E>() {
            @Override
            public Iterator<E> iterator() {
                return getDiff(set1, set2).iterator();
            }

            @Override
            public int size() {
                return getDiff(set1, set2).size();
            }

            private Set<E> getDiff(Set<? extends E> set1, Set<? extends E> set2) {
                HashSet<E> hashSet = new HashSet<>();

                hashSet.addAll(set1);
                hashSet.addAll(set2);

                return hashSet.stream().filter(new Predicate<E>() {
                    @Override
                    public boolean test(E e) {
                        return set1.contains(e) && !set2.contains(e);
                    }
                }).collect(Collectors.toSet());
            }
        };
    }
}
