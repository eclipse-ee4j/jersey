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

/**
 * Monitoring statistics of execution of any target (resource, resource method, application).
 * The main parameter of {@code TimeWindowStatistics} is the size of the time window. This is the time
 * for which the statistics are measured (for example for time window 1 hour, the statistics are evaluated
 * for last one hour and older statistics are dropped). The size of the time window can be retrieved by
 * {@link #getTimeWindow()}.
 * <p/>
 * Statistics retrieved from Jersey runtime might be mutable and thanks to it might provide inconsistent data
 * as not all statistics are updated in the same time. To retrieve the immutable and consistent
 * statistics data the method {@link #snapshot()} should be used.
 * <p/>
 *
 * @author Miroslav Fuksa
 * @see MonitoringStatistics See monitoring statistics for general details about statistics.
 */
public interface TimeWindowStatistics {

    /**
     * Returns the size of time window in milliseconds. Returned value denotes in how many last milliseconds
     * the statistics are evaluated.
     *
     * @return Time window in milliseconds.
     */
    public long getTimeWindow();

    /**
     * Returns average value of how many requests per second were received by application in the time window.
     *
     * @return Average of requests per second.
     */
    public double getRequestsPerSecond();

    /**
     * Returns the minimum duration (processing time) in milliseconds of the request processing measured
     * in the time window.
     * The time measures the
     * processing of the request since the start of request processing by Jersey until the response is
     * written or request processing fails and all resources for request processing are released.
     *
     * @return Minimum processing time of the request or -1 if no request has been processed.
     */
    public long getMinimumDuration();

    /**
     * Returns the maximum duration (processing time) in milliseconds of the request processing measured
     * in the time window.
     * processing of the request since the start of request processing by Jersey until the response is
     * written or request processing fails and all resources for request processing are released.
     *
     * @return Maximum processing time of the request or -1 if no request has been processed.
     */
    public long getMaximumDuration();

    /**
     * Returns the average duration (processing time) in milliseconds of the request processing measured
     * in the time window.
     * The time measures the
     * processing of the request since the start of request processing by Jersey until the response is
     * written or request processing fails and all resources for request processing are released.
     *
     * @return Average processing time of the request or -1 if no request has been processed.
     */
    public long getAverageDuration();

    /**
     * Returns the count of requests received measured in the time window.
     *
     * @return Count of requests that were handled by the application.
     */
    public long getRequestCount();

    /**
     * Get the immutable and consistent snapshot of the monitoring statistics. Working with snapshots might
     * have negative performance impact as snapshot must be created but ensures consistency of data over time.
     * However, the usage of snapshot is encouraged to avoid working with inconsistent data. Not all statistics
     * must be updated in the same time on mutable version of statistics.
     *
     * @return Snapshot of time window statistics.
     * @deprecated implementing class is immutable hence snapshot creation is not needed anymore
     */
    @Deprecated
    public TimeWindowStatistics snapshot();
}
