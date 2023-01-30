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

import org.glassfish.jersey.internal.guava.Cache;
import org.glassfish.jersey.internal.guava.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * An abstract LRU interface wrapping an actual LRU implementation.
 * @param <K> Key type
 * @param <V> Value type
 * @Since 2.38
 */
public abstract class LRU<K, V> {

    /**
     * Returns the value associated with {@code key} in this cache, or {@code null} if there is no
     * cached value for {@code key}.
     */
    public abstract V getIfPresent(Object key);

    /**
     * Associates {@code value} with {@code key} in this cache. If the cache previously contained a
     * value associated with {@code key}, the old value is replaced by {@code value}.
     */
    public abstract void put(K key, V value);

    /**
     * Create new LRU
     * @return new LRU
     */
    public static <K, V> LRU<K, V> create() {
        return LRUFactory.createLRU();
    }

    private static class LRUFactory {
        // TODO configure via the Configuration
        public static final int LRU_CACHE_SIZE = 128;
        public static final long TIMEOUT = 5000L;
        private static <K, V> LRU<K, V> createLRU() {
            final Cache<K, V> CACHE = CacheBuilder.newBuilder()
                    .maximumSize(LRU_CACHE_SIZE)
                    .expireAfterAccess(TIMEOUT, TimeUnit.MILLISECONDS)
                    .build();
            return new LRU<K, V>() {
                @Override
                public V getIfPresent(Object key) {
                    return CACHE.getIfPresent(key);
                }

                @Override
                public void put(K key, V value) {
                    CACHE.put(key, value);
                }
            };
        }
    }


}
