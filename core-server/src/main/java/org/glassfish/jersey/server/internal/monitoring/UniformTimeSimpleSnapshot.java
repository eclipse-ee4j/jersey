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
import org.glassfish.jersey.server.internal.monitoring.core.AbstractTimeSnapshot;

/**
 * A statistical snapshot of a {@link UniformTimeSimpleSnapshot}.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 * @author Dropwizard Team
 * @see <a href="https://github.com/dropwizard/metrics">https://github.com/dropwizard/metrics</a>
 */
class UniformTimeSimpleSnapshot extends AbstractTimeSnapshot {


    private final long max;
    private final long min;
    private final double mean;
    private final long count;

    /**
     * Constructs the snapshot which simply returns the provided data as arguments.
     *
     * @param max              The maximum.
     * @param min              The minimum.
     * @param mean             The mean.
     * @param count            The total count.
     * @param timeInterval     The time interval of this snapshot.
     * @param timeIntervalUnit The time interval unit.
     */
    public UniformTimeSimpleSnapshot(final long max,
                                     final long min,
                                     final double mean,
                                     final long count,
                                     final long timeInterval,
                                     final TimeUnit timeIntervalUnit) {
        super(timeInterval, timeIntervalUnit);
        this.max = max;
        this.min = min;
        this.mean = mean;
        this.count = count;
    }

    @Override
    public long size() {
        return count;
    }

    @Override
    public long getMax() {
        return max;
    }

    @Override
    public long getMin() {
        return min;
    }

    @Override
    public double getMean() {
        return mean;
    }
}
