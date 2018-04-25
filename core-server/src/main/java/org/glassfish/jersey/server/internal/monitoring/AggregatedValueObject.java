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

package org.glassfish.jersey.server.internal.monitoring;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Aggregated value object stores aggregated measurements for provided set of data. The purpose of aggregation is to avoid high
 * memory and processor time requirements for the calculation of statistics.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
class AggregatedValueObject {

    private final long max;
    private final long min;
    private final double mean;
    private final long count;

    private AggregatedValueObject(final long max, final long min, final double mean, final long count) {
        this.max = max;
        this.min = min;
        this.mean = mean;
        this.count = count;
    }

    /**
     * Creates aggregated value object for monitoring statistics based on the provided values. During the construction, the values
     * collection must not be modified.
     *
     * @param values The collection to create the aggregated statistics from.
     * @return Aggregated value object for provided arguments.
     */
    public static AggregatedValueObject createFromValues(Collection<Long> values) {
        if (values.isEmpty()) {
            // aggregated objects must be created for at least one value, additionally, prevent from division by zero in the mean
            throw new IllegalArgumentException("The values collection must not be empty");
        }

        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        long sum = 0;
        for (Long value : values) {
            max = Math.max(max, value);
            min = Math.min(min, value);
            sum += value;
        }

        return new AggregatedValueObject(max, min, (double) sum / values.size(), values.size());
    }

    /**
     * Creates aggregated value object for monitoring statistics based on the provided collection of values. During the
     * construction, the values collection must not be modified.
     *
     * @param values The collection to create the aggregated statistics from.
     * @return Aggregated value object for provided arguments.
     */
    public static AggregatedValueObject createFromMultiValues(Collection<? extends Collection<Long>> values) {
        final Collection<Long> mergedCollection = new LinkedList<>();
        for (Collection<Long> collection : values) {
            mergedCollection.addAll(collection);
        }
        return createFromValues(mergedCollection);
    }

    /**
     * @return The maximum value of the aggregated data
     */
    public long getMax() {
        return max;
    }

    /**
     * @return The minimum value of the aggregated data
     */
    public long getMin() {
        return min;
    }

    /**
     * @return The mean of the aggregated data
     */
    public double getMean() {
        return mean;
    }

    /**
     * @return The total number of the values this aggregated data provide information about
     */
    public long getCount() {
        return count;
    }
}
