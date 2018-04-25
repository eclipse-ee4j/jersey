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
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glassfish.jersey.internal.guava.TreeMultimap;
import org.glassfish.jersey.server.internal.monitoring.core.SlidingWindowTrimmer;
import org.glassfish.jersey.server.internal.monitoring.core.TimeReservoir;

import static org.glassfish.jersey.server.internal.monitoring.core.ReservoirConstants.COLLISION_BUFFER_POWER;

/**
 * An aggregating trimmer for sliding window measurements. This trimmer updates registered time reservoirs with the aggregated
 * measurements for the values it trimmed.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
class AggregatingTrimmer implements SlidingWindowTrimmer<Long> {

    private final List<TimeReservoir<AggregatedValueObject>> aggregatedReservoirListeners = new CopyOnWriteArrayList<>();
    private TimeReservoir<Long> timeReservoirNotifier;

    private final long startTime;
    private final TimeUnit startUnitTime;
    private final long chunkSize;

    /**
     * The lock that prevents other threads to trim the associated reservoir in parallel.
     */
    private final AtomicBoolean locked = new AtomicBoolean(false);

    /**
     * Creates the trimmer that updates the registered time reservoirs with the aggregated measurements for the values it
     * trimmed.
     *
     * @param startTime         The start time that determines the offset for the chunks.
     * @param startUnitTime     The time unit of the start time.
     * @param chunkTimeSize     The size of one "time chunk".
     * @param chunkTimeSizeUnit The time unit of the time chunk.
     */
    public AggregatingTrimmer(final long startTime,
                              final TimeUnit startUnitTime,
                              final long chunkTimeSize,
                              final TimeUnit chunkTimeSizeUnit) {
        this.startTime = startTime;
        this.startUnitTime = startUnitTime;
        this.chunkSize = TimeUnit.NANOSECONDS.convert(chunkTimeSize, chunkTimeSizeUnit) << COLLISION_BUFFER_POWER;
    }

    @Override
    public void trim(final ConcurrentNavigableMap<Long, Long> map, final long key) {
        if (!locked.compareAndSet(false, true)) {
            return;
        }
        final TreeMultimap<Long, Long> trimMultiMap = TreeMultimap.create();
        final NavigableMap<Long, Collection<Long>> trimMap = trimMultiMap.asMap();

        try {
            final ConcurrentNavigableMap<Long, Long> headMap = map.headMap(key);
            while (!headMap.isEmpty()) {
                // headMap itself is being accessed with updates from other threads
                final Map.Entry<Long, Long> entry = headMap.pollFirstEntry();
                trimMultiMap.put(entry.getKey(), entry.getValue());
            }
            // now the headMap is trimmed...
        } finally {
            locked.set(false);
        }

        for (Map.Entry<Long, Collection<Long>> firstEntry = trimMap.firstEntry(); firstEntry != null;
                firstEntry = trimMap.firstEntry()) {
            long chunkLowerBound = lowerBound(firstEntry.getKey());
            long chunkUpperBound = upperBound(chunkLowerBound, key);

            // now, we need to process and then remove entries at interval [chunkLowerBound, chunkUpperBound)
            SortedMap<Long, Collection<Long>> chunkMap = trimMap.headMap(chunkUpperBound);

            final AggregatedValueObject aggregatedValueObject = AggregatedValueObject.createFromMultiValues(chunkMap.values());

            // update all listening aggregated reservoirs
            for (TimeReservoir<AggregatedValueObject> aggregatedReservoir : aggregatedReservoirListeners) {
                aggregatedReservoir
                        .update(aggregatedValueObject, chunkLowerBound >> COLLISION_BUFFER_POWER, TimeUnit.NANOSECONDS);
            }

            // clean up the chunk, which also removes its items from the 'trimMap'
            chunkMap.clear();
        }
    }

    private long upperBound(final long chunkLowerBound, final long key) {
        final long chunkUpperBoundCandidate = chunkLowerBound + chunkSize;
        return chunkUpperBoundCandidate < key ? chunkUpperBoundCandidate : key;
    }

    private long lowerBound(final Long key) {
        return lowerBound(key, TimeUnit.NANOSECONDS.convert(startTime, startUnitTime), chunkSize,
                COLLISION_BUFFER_POWER);
    }

    /**
     * Calculates lower bound for given key so that following conditions are true
     * <pre><ul>
     *     <li>{@code lowerBound <= key && key < lowerBound + chunkSize}</li>
     *     <li>The lower bound is a multiple of chunk size with an offset calculated as {@code (startTime % chunkSize) <<
     * power}</li>
     * </ul></pre>
     * Note the offset calculation is determined by start time because not always one lower bound from the sequence of all lower
     * bounds for given arguments is equal to 0.<br/> The power is used to shift the offset because all the keys are also expected
     * to be shifted with the power.
     *
     * @param key       The key to find the lower bound for.
     * @param startTime The start time that determines the offset for the chunks.
     * @param chunkSize The size of one chunk.
     * @param power     The power the keys are expected to be shifted with.
     * @return The lower bound for given arguments satisfying conditions stated above.
     */
    static long lowerBound(final long key, final long startTime, final long chunkSize, final int power) {
        final long offset = (startTime % chunkSize) << power;
        if (key - offset >= 0) {
            return ((key - offset) / chunkSize * chunkSize) + offset;
        } else {
            return ((key - offset - chunkSize + 1)) / chunkSize * chunkSize + offset;
        }
    }

    /**
     * Registers given aggregating sliding window reservoir to get updates from this trimmer.
     *
     * @param timeReservoirListener The aggregated sliding window reservoir to update with trimmed measurements
     */
    public void register(final TimeReservoir<AggregatedValueObject> timeReservoirListener) {
        aggregatedReservoirListeners.add(timeReservoirListener);
    }

    @Override
    public void setTimeReservoir(final TimeReservoir<Long> timeReservoirNotifier) {
        this.timeReservoirNotifier = timeReservoirNotifier;
    }

    /**
     * @return The reservoir that produces the data this trimmer aggregates and trims.
     */
    public TimeReservoir<Long> getTimeReservoirNotifier() {
        return timeReservoirNotifier;
    }
}
