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

import org.glassfish.jersey.server.internal.monitoring.core.TimeReservoir;
import org.glassfish.jersey.server.internal.monitoring.core.UniformTimeSnapshot;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class AbstractNanosReservoirTest {

    protected static final double DELTA = 0.0001;
    protected static final int COLLISION_BUFFER = 256;

    protected void reservoirUpdateInNanos(TimeReservoir reservoir, long value, long time) {
        reservoir.update(value, time, TimeUnit.NANOSECONDS);
    }

    protected void checkInNanos(final TimeReservoir reservoir,
                                final long snapshotTime,
                                final int expectedSize,
                                final int expectedMin,
                                final int expectedMax,
                                final double expectedMean) {
        checkInNanos(reservoir, snapshotTime, expectedSize, expectedMin, expectedMax, expectedMean,
                reservoir.getSnapshot(snapshotTime, TimeUnit.NANOSECONDS).getTimeInterval(TimeUnit.NANOSECONDS));
    }

    /**
     * Checks whether the snapshot of given reservoir exhibits with expected measurements.
     *
     * @param reservoir        The reservoir to assert.
     * @param snapshotTime     The time for which to get the snapshot
     * @param expectedSize     Expected size of the snapshot
     * @param expectedMin      Expected minimum
     * @param expectedMax      Expected maximum
     * @param expectedMean     Expected mean
     * @param expectedInterval Expected interval
     */
    protected void checkInNanos(final TimeReservoir reservoir,
                                final long snapshotTime,
                                final long expectedSize,
                                final long expectedMin,
                                final long expectedMax,
                                final double expectedMean, final long expectedInterval) {
        final UniformTimeSnapshot snapshot = reservoir.getSnapshot(snapshotTime, TimeUnit.NANOSECONDS);

        assertEquals("Total count does not match!", expectedSize, snapshot.size());
        assertEquals("Min exec time does not match!", expectedMin, snapshot.getMin());
        assertEquals("Max exec time does not match!", expectedMax, snapshot.getMax());
        assertEquals("Average exec time does not match!", expectedMean, snapshot.getMean(), DELTA);
        assertEquals("Expected interval does not match!", expectedInterval, snapshot.getTimeInterval(TimeUnit.NANOSECONDS));
    }

}
