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

package org.glassfish.jersey.server.monitoring;

import java.util.Date;
import java.util.Map;

/**
 * Monitoring statistics of execution of one target. {@code ExecutionStatistics} contains
 * {@link TimeWindowStatistics} for various time window sizes.
 * <p/>
 * Statistics retrieved from Jersey runtime might be mutable and thanks to it might provide inconsistent data
 * as not all statistics are updated in the same time. To retrieve the immutable and consistent
 * statistics data the method {@link #snapshot()} should be used.
 *
 * @author Miroslav Fuksa
 * @see MonitoringStatistics See monitoring statistics for general details about statistics.
 */
public interface ExecutionStatistics {

    /**
     * Return time when target was executed last time. The time is measured before the target was executed.
     *
     * @return Time of last execution.
     */
    public Date getLastStartTime();

    /**
     * Returns time window statistics for available time window sizes. The returned map contains sizes
     * of a time window in milliseconds as keys and
     * {@link TimeWindowStatistics time window statistics} for the corresponding time window
     * as value.
     *
     * @return Map with size of a time window in milliseconds as keys and
     *         {@link TimeWindowStatistics time window statistics} for the corresponding time window
     *         as value.
     */
    public Map<Long, TimeWindowStatistics> getTimeWindowStatistics();

    /**
     * Get the immutable consistent snapshot of the monitoring statistics. Working with snapshots might
     * have negative performance impact as snapshot must be created but ensures consistency of data over time.
     * However, the usage of snapshot is encouraged to avoid working with inconsistent data. Not all statistics
     * must be updated in the same time on mutable version of statistics.
     *
     * @return Snapshot of execution statistics.
     * @deprecated implementing class is immutable hence snapshot creation is not needed anymore
     */
    @Deprecated
    public ExecutionStatistics snapshot();
}
