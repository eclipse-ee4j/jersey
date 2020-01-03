/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.server.internal.monitoring.core.UniformTimeReservoir;
import org.glassfish.jersey.server.monitoring.ExecutionStatistics;
import org.glassfish.jersey.server.monitoring.TimeWindowStatistics;

/**
 * Immutable Execution statistics.
 *
 * @author Miroslav Fuksa
 * @author Stepan Vavra
 */
final class ExecutionStatisticsImpl implements ExecutionStatistics {

    /**
     * Empty execution statistics instance.
     */
    static final ExecutionStatistics EMPTY = new Builder().build();

    /**
     * Builder of execution statistics.
     * <p/>
     * Must be thread-safe.
     */
    static class Builder {

        private volatile long lastStartTime;
        private final Map<Long, TimeWindowStatisticsImpl.Builder> intervalStatistics;
        private final Collection<TimeWindowStatisticsImpl.Builder<Long>> updatableIntervalStatistics;

        /**
         * Create a new builder.
         */
        @SuppressWarnings("MagicNumber")
        public Builder() {
            final long nowMillis = System.currentTimeMillis();
            final AggregatingTrimmer trimmer = new AggregatingTrimmer(nowMillis, TimeUnit.MILLISECONDS, 1, TimeUnit.SECONDS);
            final TimeWindowStatisticsImpl.Builder<Long> oneSecondIntervalWindowBuilder =
                    new TimeWindowStatisticsImpl.Builder<>(
                            new SlidingWindowTimeReservoir(1, TimeUnit.SECONDS, nowMillis, TimeUnit.MILLISECONDS, trimmer));
            final TimeWindowStatisticsImpl.Builder<Long> infiniteIntervalWindowBuilder =
                    new TimeWindowStatisticsImpl.Builder<>(new UniformTimeReservoir(nowMillis, TimeUnit.MILLISECONDS));

            this.updatableIntervalStatistics =
                    Arrays.asList(infiniteIntervalWindowBuilder, oneSecondIntervalWindowBuilder);

            // create unmodifiable map to ensure that an iteration in the build() won't have multi-threading issues
            final HashMap<Long, TimeWindowStatisticsImpl.Builder> tmpIntervalStatistics = new HashMap<>(6);
            // Add approximate infinite time window builder
            tmpIntervalStatistics.put(0L, infiniteIntervalWindowBuilder);
            // Add precise 1 second time window builder
            tmpIntervalStatistics.put(TimeUnit.SECONDS.toMillis(1), oneSecondIntervalWindowBuilder);
            // Add aggregated 15 seconds time window builder
            addAggregatedInterval(tmpIntervalStatistics, nowMillis, 15, TimeUnit.SECONDS, trimmer);
            // Add aggregated 1 minute time window builder
            addAggregatedInterval(tmpIntervalStatistics, nowMillis, 1, TimeUnit.MINUTES, trimmer);
            // Add aggregated 15 minutes time window builder
            addAggregatedInterval(tmpIntervalStatistics, nowMillis, 15, TimeUnit.MINUTES, trimmer);
            // Add aggregated 1 hour time window builder
            addAggregatedInterval(tmpIntervalStatistics, nowMillis, 1, TimeUnit.HOURS, trimmer);

            this.intervalStatistics = Collections.unmodifiableMap(tmpIntervalStatistics);
        }

        private static void addAggregatedInterval(
                final Map<Long, TimeWindowStatisticsImpl.Builder> intervalStatisticsMap,
                final long nowMillis,
                final long interval,
                final TimeUnit timeUnit,
                final AggregatingTrimmer notifier) {
            final long intervalInMillis = timeUnit.toMillis(interval);
            intervalStatisticsMap.put(intervalInMillis, new TimeWindowStatisticsImpl.Builder<>(
                    new AggregatedSlidingWindowTimeReservoir(intervalInMillis, TimeUnit.MILLISECONDS, nowMillis,
                            TimeUnit.MILLISECONDS, notifier)));
        }

        /**
         * Add execution of a target.
         *
         * @param startTime Start time of an execution event (in Unix timestamp format).
         * @param duration  Duration of an execution event in milliseconds.
         */
        void addExecution(final long startTime, final long duration) {
            for (final TimeWindowStatisticsImpl.Builder<Long> statBuilder : updatableIntervalStatistics) {
                statBuilder.addRequest(startTime, duration);
            }

            this.lastStartTime = startTime;
        }

        /**
         * Build a new instance of execution statistics.
         *
         * @return new instance of execution statistics.
         */
        public ExecutionStatisticsImpl build() {
            final Map<Long, TimeWindowStatistics> newIntervalStatistics = new HashMap<>();
            for (final Map.Entry<Long, TimeWindowStatisticsImpl.Builder> builderEntry : intervalStatistics.entrySet()) {
                newIntervalStatistics.put(builderEntry.getKey(), builderEntry.getValue().build());
            }

            // cache when request rate is 0

            return new ExecutionStatisticsImpl(lastStartTime, newIntervalStatistics);
        }
    }

    private final long lastStartTime;
    private final Map<Long, TimeWindowStatistics> timeWindowStatistics;

    @Override
    public Date getLastStartTime() {
        return new Date(lastStartTime);
    }

    @Override
    public Map<Long, TimeWindowStatistics> getTimeWindowStatistics() {
        return timeWindowStatistics;
    }

    @Override
    public ExecutionStatistics snapshot() {
        // this object is immutable (TimeWindowStatistics are immutable as well)
        return this;
    }

    private ExecutionStatisticsImpl(final long lastStartTime, final Map<Long, TimeWindowStatistics> timeWindowStatistics) {
        this.lastStartTime = lastStartTime;
        this.timeWindowStatistics = Collections.unmodifiableMap(timeWindowStatistics);
    }

}
