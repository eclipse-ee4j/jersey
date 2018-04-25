/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.server.internal.monitoring.core.TimeReservoir;
import org.glassfish.jersey.server.internal.monitoring.core.UniformTimeSnapshot;
import org.glassfish.jersey.server.monitoring.TimeWindowStatistics;

/**
 * Immutable {@link TimeWindowStatistics Time window statistics} that uses backing {@link SlidingWindowTimeReservoir} for its
 * {@code Builder} implementation.
 *
 * @author Miroslav Fuksa
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
final class TimeWindowStatisticsImpl implements TimeWindowStatistics {

    /**
     * Builder of time window statistics.
     */
    static class Builder<V> {

        /**
         * Total interval for which these statistics are calculated (eg. last 15 seconds, last one minute) converted to ms
         */
        private final long interval;

        private final TimeReservoir<V> timeReservoir;

        /**
         * Create new time window statistics builder instance.
         *
         * @param timeReservoir statistically representative reservoir of long values data stream in time.
         */
        Builder(TimeReservoir<V> timeReservoir) {
            interval = timeReservoir.interval(TimeUnit.MILLISECONDS);
            this.timeReservoir = timeReservoir;
        }

        /**
         * Add request execution.
         *
         * @param requestTime Time of execution.
         * @param duration    Duration of request processing.
         */
        void addRequest(final long requestTime, final V duration) {
            timeReservoir.update(duration, requestTime, TimeUnit.MILLISECONDS);
        }

        /**
         * Build the time window statistics instance.
         *
         * @return New instance of statistics.
         */
        TimeWindowStatisticsImpl build() {
            return build(System.currentTimeMillis());
        }

        /**
         * Build the time window statistics instance.
         *
         * @param currentTime Current time as a reference to which the statistics should be built.
         * @return New instance of statistics.
         */
        TimeWindowStatisticsImpl build(final long currentTime) {
            final UniformTimeSnapshot durationReservoirSnapshot = timeReservoir
                    .getSnapshot(currentTime, TimeUnit.MILLISECONDS);

            // if nothing was collected, return a single empty stat instance
            if (durationReservoirSnapshot.size() == 0) {
                return getOrCreateEmptyStats(interval);
            }

            return new TimeWindowStatisticsImpl(interval, durationReservoirSnapshot);

        }

        private TimeWindowStatisticsImpl getOrCreateEmptyStats(final long interval) {
            if (!EMPTY.containsKey(interval)) {
                EMPTY.putIfAbsent(interval, new TimeWindowStatisticsImpl(interval, 0, -1, -1, -1, 0));
            }
            return EMPTY.get(interval);
        }

        public long getInterval() {
            return interval;
        }
    }

    private static final ConcurrentHashMap<Long, TimeWindowStatisticsImpl> EMPTY = new ConcurrentHashMap<>(6);

    static {
        EMPTY.putIfAbsent(0L, new TimeWindowStatisticsImpl(0, 0, 0, 0, 0, 0));
    }

    private final long interval;

    private final long minimumDuration;
    private final long maximumDuration;
    private final long averageDuration;

    private final long totalCount;
    private final double requestsPerSecond;

    private TimeWindowStatisticsImpl(final long interval, final double requestsPerSecond, final long minimumDuration,
                                     final long maximumDuration, final long averageDuration, final long totalCount) {
        this.interval = interval;
        this.requestsPerSecond = requestsPerSecond;
        this.minimumDuration = minimumDuration;
        this.maximumDuration = maximumDuration;
        this.averageDuration = averageDuration;
        this.totalCount = totalCount;
    }

    private TimeWindowStatisticsImpl(final long interval, final UniformTimeSnapshot snapshot) {
        this(interval, snapshot.getRate(TimeUnit.SECONDS), snapshot.getMin(), snapshot.getMax(), (long) snapshot.getMean(),
                snapshot.size());
    }

    @Override
    public long getTimeWindow() {
        return interval;
    }

    @Override
    public double getRequestsPerSecond() {
        return requestsPerSecond;
    }

    @Override
    public long getMinimumDuration() {
        return minimumDuration;
    }

    @Override
    public long getMaximumDuration() {
        return maximumDuration;
    }

    @Override
    public long getRequestCount() {
        return totalCount;
    }

    @Override
    public TimeWindowStatistics snapshot() {
        // TimeWindowStatisticsImpl is immutable; the Builder is mutable
        return this;
    }

    @Override
    public long getAverageDuration() {
        return averageDuration;
    }
}
