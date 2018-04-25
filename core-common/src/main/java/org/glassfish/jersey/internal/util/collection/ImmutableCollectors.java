/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

/**
 * Set of convenient function regarding a collection immutability. Particularly useful in the conjunction with
 * {@link java.util.stream.Stream}.
 */
public class ImmutableCollectors {

    /**
     * Creates a {@link Collector} of an immutable list for {@link java.util.stream.Stream#collect(Collector)}.
     *
     * @param <T> type of the immutable list.
     * @return collector for immutable list.
     */
    public static <T> Collector<T, List<T>, List<T>> toImmutableList() {
        return Collector.of(ArrayList::new, List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, Collections::unmodifiableList);
    }

    /**
     * Creates a {@link Collector} of an immutable Set for {@link java.util.stream.Stream#collect(Collector)}.
     *
     * @param <T> type of the immutable set.
     * @return collector for immutable set.
     */
    public static <T> Collector<T, Set<T>, Set<T>> toImmutableSet() {
        return Collector.of(HashSet::new, Set::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, Collections::unmodifiableSet);
    }

    /**
     * Creates a {@link Collector} of an immutable Set for {@link java.util.stream.Stream#collect(Collector)}.
     *
     * @param <T> type of the immutable linked hash set.
     * @return collector for immutable linked hash set.
     */
    public static <T> Collector<T, Set<T>, Set<T>> toImmutableLinkedSet() {
        return Collector.of(LinkedHashSet::new, Set::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, Collections::unmodifiableSet);
    }
}
