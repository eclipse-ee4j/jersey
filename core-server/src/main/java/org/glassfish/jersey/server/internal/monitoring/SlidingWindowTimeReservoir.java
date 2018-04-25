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
import org.glassfish.jersey.server.internal.monitoring.core.SlidingWindowTrimmer;
import org.glassfish.jersey.server.internal.monitoring.core.UniformTimeSnapshot;
import org.glassfish.jersey.server.internal.monitoring.core.UniformTimeValuesSnapshot;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Sliding window time reservoir implementation that stores data of type {@link Long}.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
class SlidingWindowTimeReservoir extends AbstractSlidingWindowTimeReservoir<Long> {

    /**
     * Creates a new sliding window time reservoir with the start time, specified time window and a custom trimmer.
     *
     * @param window        The window of startTime.
     * @param windowUnit    The unit of {@code window}.
     * @param startTime     The start time from which this reservoir calculates measurements.
     * @param startTimeUnit The start time unit.
     * @param trimmer       The trimmer to use for trimming, if {@code null}, default trimmer is used.
     */
    public SlidingWindowTimeReservoir(final long window,
                                      final TimeUnit windowUnit,
                                      final long startTime,
                                      final TimeUnit startTimeUnit,
                                      final SlidingWindowTrimmer<Long> trimmer) {
        super(window, windowUnit, startTime, startTimeUnit, trimmer);
    }

    /**
     * Creates a new sliding window time reservoir with the start time, specified time window and a default trimmer.
     *
     * @param window        The window of startTime.
     * @param windowUnit    The unit of {@code window}.
     * @param startTime     The start time from which this reservoir calculates measurements.
     * @param startTimeUnit The start time unit.
     */
    public SlidingWindowTimeReservoir(final long window,
                                      final TimeUnit windowUnit,
                                      final long startTime,
                                      final TimeUnit startTimeUnit) {
        this(window, windowUnit, startTime, startTimeUnit, null);
    }

    @Override
    protected UniformTimeSnapshot snapshot(final Collection<Long> values,
                                           final long timeInterval,
                                           final TimeUnit timeIntervalUnit,
                                           final long time,
                                           final TimeUnit timeUnit) {
        return new UniformTimeValuesSnapshot(values, timeInterval, timeIntervalUnit);
    }
}
