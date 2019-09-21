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

package org.glassfish.jersey.model.internal;

import java.util.Comparator;

/**
 * Comparator used to sort types by their priorities defined by theirs binding priority set during the configuration phase
 * ({@link javax.ws.rs.core.Configuration}) in {@link RankedProvider ranked provider}.
 *
 * @param <T> Type of the elements to be sorted.
 *
 * @author Miroslav Fuksa
 * @author Michal Gajdos
 */
public class RankedComparator<T> implements Comparator<RankedProvider<T>> {

    /**
     * Defines which ordering should be used for sorting.
     */
    public enum Order {
        /**
         * Ascending order. The lowest priority first, the highest priority last.
         */
        ASCENDING(1),
        /**
         * Ascending order. The highest priority first, the lowest priority last.
         */
        DESCENDING(-1);

        private final int ordering;

        private Order(int ordering) {
            this.ordering = ordering;
        }
    }

    private final Order order;

    public RankedComparator() {
        this(Order.ASCENDING);
    }

    public RankedComparator(final Order order) {
        this.order = order;
    }

    @Override
    public int compare(final RankedProvider<T> o1, final RankedProvider<T> o2) {
        return ((getPriority(o1) > getPriority(o2)) ? order.ordering : -order.ordering);
    }

    protected int getPriority(final RankedProvider<T> rankedProvider) {
        return rankedProvider.getRank();
    }
}
