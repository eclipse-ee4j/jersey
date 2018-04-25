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

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class AggregatingTrimmerTest extends AbstractNanosReservoirTest {

    private final long startTime = System.nanoTime();
    private final TimeUnit startUnitTime = TimeUnit.NANOSECONDS;
    private final AggregatingTrimmer trimmer = new AggregatingTrimmer(startTime(), startUnitTime, 10, TimeUnit.NANOSECONDS);
    private final SlidingWindowTimeReservoir time10nsReservoir = new SlidingWindowTimeReservoir(10, TimeUnit.NANOSECONDS,
            startTime(), startUnitTime, trimmer);
    private final AggregatedSlidingWindowTimeReservoir aggregatedTime100nsReservoir = new
            AggregatedSlidingWindowTimeReservoir(100, TimeUnit.NANOSECONDS, startTime(), startUnitTime, trimmer);

    protected long startTime() {
        return startTime;
    }

    @Test
    public void simpleCheck() {

        time10nsReservoir.update(10L, startTime(), startUnitTime);
        time10nsReservoir.update(20L, startTime() + 50, startUnitTime);

        checkInNanos(aggregatedTime100nsReservoir, startTime() + 100, 2, 10, 20, 15);
    }

    @Test
    public void trimSlidingWindowBeforeAggregatedWindow() {

        time10nsReservoir.update(10L, startTime(), startUnitTime);
        time10nsReservoir.update(20L, startTime() + 50, startUnitTime);

        checkInNanos(time10nsReservoir, startTime() + 100, 0, 0, 0, 0);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 100, 2, 10, 20, 15);
    }

    @Test
    public void testAggregatingTrimmer() {

        time10nsReservoir.update(10L, startTime(), startUnitTime);
        time10nsReservoir.update(20L, startTime() + 50, startUnitTime);

        checkInNanos(time10nsReservoir, startTime() + 50, 1, 20, 20, 20);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 100, 2, 10, 20, 15);
    }

    @Test
    public void testAggregatingTrimmerDoubleValues() {
        checkInNanos(aggregatedTime100nsReservoir, startTime(), 0, 0, 0, 0);

        time10nsReservoir.update(1L, startTime(), startUnitTime);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 5, 1, 1, 1, 1);

        time10nsReservoir.update(2L, startTime() + 5, startUnitTime);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 5, 2, 1, 2, 1.5);

        time10nsReservoir.update(5L, startTime() + 11, startUnitTime);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 12, 3, 1, 5, 2.6666);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 30, 3, 1, 5, 2.6666);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 100, 3, 1, 5, 2.6666);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 101, 1, 5, 5, 5);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 110, 1, 5, 5, 5);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 111, 0, 0, 0, 0);
    }

    @Test
    public void testAggregatingTrimmerMultipleDoubleValuesInOneChunk() {
        // go to the first chunk
        time10nsReservoir.update(1L, startTime() + 5, startUnitTime);
        time10nsReservoir.update(2L, startTime() + 6, startUnitTime);

        // go to the second chunk
        time10nsReservoir.update(6L, startTime() + 11, startUnitTime);
        time10nsReservoir.update(3L, startTime() + 11, startUnitTime);
        time10nsReservoir.update(11L, startTime() + 14, startUnitTime);

        checkInNanos(aggregatedTime100nsReservoir, startTime() + 14, 5, 1, 11, 23d / 5);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 14, 5, 1, 11, 23d / 5);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 15, 5, 1, 11, 23d / 5);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 16, 5, 1, 11, 23d / 5);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 100, 5, 1, 11, 23d / 5);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 101, 3, 3, 11, 20d / 3);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 110, 3, 3, 11, 20d / 3);
        checkInNanos(aggregatedTime100nsReservoir, startTime() + 111, 0, 0, 0, 0);
    }

    @Test
    public void testLowerBoundFunction() {
        for (long chunkSize = 1; chunkSize < 15; ++chunkSize) {
            for (int power = 0; power < 8; ++power) {
                for (int startTime = -50; startTime < 50; ++startTime) {
                    for (int i = -50; i < 50; ++i) {
                        long lowerBound = AggregatingTrimmer.lowerBound(i, startTime, chunkSize, power);
                        Assert.assertTrue("Error occurred for: " + i + " .. lower bound: " + lowerBound + " .. power: " + power
                                        + " .. startTime: " + startTime,
                                lowerBound <= i);
                        Assert.assertTrue("Error occurred for: " + i + " .. lower bound: " + lowerBound + " .. power: " + power
                                        + " .. startTime: " + startTime,
                                i < lowerBound + chunkSize);
                    }
                }
            }
        }
    }
}
