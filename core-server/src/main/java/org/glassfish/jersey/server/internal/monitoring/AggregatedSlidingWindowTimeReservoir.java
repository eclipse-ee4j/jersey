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

import org.glassfish.jersey.server.internal.monitoring.core.AbstractSlidingWindowTimeReservoir;
import org.glassfish.jersey.server.internal.monitoring.core.UniformTimeSnapshot;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Aggregated sliding window time reservoir stores aggregated measurements in a time window of given size. The resulting snapshot
 * provides precise data as far as the granularity of aggregating trimmer is not concerned. The granularity of the trimmer
 * determines the granularity of the data the snapshot provides. In other words, the aggregated value object is either included in
 * the resulting measurements or not depending whether it was trimmed or not.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
class AggregatedSlidingWindowTimeReservoir extends AbstractSlidingWindowTimeReservoir<AggregatedValueObject> {

    private final AggregatingTrimmer notifier;

    /**
     * Creates an aggregated sliding window reservoir.
     *
     * @param window The time size of the window
     * @param windowUnit The unit of the window size
     * @param startTime The start time from when to calculate the statistics
     * @param startTimeUnit The unit of the start time
     * @param notifier The aggregating trimmer that produces the aggregated data
     */
    public AggregatedSlidingWindowTimeReservoir(
            final long window,
            final TimeUnit windowUnit,
            final long startTime,
            final TimeUnit startTimeUnit, final AggregatingTrimmer notifier) {
        super(window, windowUnit, startTime, startTimeUnit);
        this.notifier = notifier;
        notifier.register(this);
    }

    @Override
    protected UniformTimeSnapshot snapshot(final Collection<AggregatedValueObject> values,
                                           final long timeInterval,
                                           final TimeUnit timeIntervalUnit,
                                           final long time,
                                           final TimeUnit timeUnit) {
        final UniformTimeSnapshot notTrimmedMeasurementsSnapshot = notifier.getTimeReservoirNotifier()
                .getSnapshot(time, timeUnit);

        AggregatedValueObject[] arrayValues = new AggregatedValueObject[values.size()];
        arrayValues = values.toArray(arrayValues);
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long count = 0;
        double meanNumerator = 0;

        for (AggregatedValueObject value : arrayValues) {
            min = Math.min(min, value.getMin());
            max = Math.max(max, value.getMax());
            count += value.getCount();
            meanNumerator += value.getCount() * value.getMean();
        }
        if (notTrimmedMeasurementsSnapshot.size() > 0) {
            min = Math.min(min, notTrimmedMeasurementsSnapshot.getMin());
            max = Math.max(max, notTrimmedMeasurementsSnapshot.getMax());
            count += notTrimmedMeasurementsSnapshot.size();
            meanNumerator += notTrimmedMeasurementsSnapshot.size() * notTrimmedMeasurementsSnapshot.getMean();
        }

        if (count == 0) {
            return new UniformTimeSimpleSnapshot(0, 0, 0, 0, timeInterval, timeIntervalUnit);
        } else {
            return new UniformTimeSimpleSnapshot(max, min, meanNumerator / count, count, timeInterval, timeIntervalUnit);
        }
    }

}
