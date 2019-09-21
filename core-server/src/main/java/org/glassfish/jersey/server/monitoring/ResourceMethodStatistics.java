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

import org.glassfish.jersey.server.model.ResourceMethod;

/**
 * Monitoring statistics an of an execution of the resource method. The {@link #snapshot()}
 * method returns an immutable snapshot with consistent data. The principles of using statistics
 * is similar to principles of using {@link MonitoringStatistics}.
 * <p/>
 * Statistics contain two {@link ExecutionStatistics} where {@link #getMethodStatistics()} contains
 * statistics for execution of the code of resource method and {@link #getRequestStatistics()} contains
 * statistics for complete processing of requests that were matched to the resource method. This implies that
 * {@link #getRequestStatistics()} will tend to contain higher time measurements as they measure total request
 * processing time and not only execution of the resource method.
 * <p/>
 * Statistics retrieved from Jersey runtime might be mutable and thanks to it might provide inconsistent data
 * as not all statistics are updated in the same time. To retrieve the immutable and consistent
 * statistics data the method {@link #snapshot()} should be used.
 *
 * @author Miroslav Fuksa
 * @see MonitoringStatistics See monitoring statistics for more details.
 */
public interface ResourceMethodStatistics {

    /**
     * Get {@link ExecutionStatistics execution statistics} that contain measurements of times only for
     * execution of resource method. Durations average time, minimum time and maximum time
     * measure only time of execution of resource method code. It does not involve other request processing
     * phases.
     *
     * @return Execution statistics of one resource method.
     */
    public ExecutionStatistics getMethodStatistics();


    /**
     * Get {@link ExecutionStatistics execution statistics} that contain measurements of times for
     * whole processing from time when request comes into the Jersey application until the response
     * is written to the underlying IO container. The statistics involves only requests that were matched
     * to resource method defined by {@link #getResourceMethod()}.
     *
     * @return Execution statistics of entire request processing for one resource method.
     */
    public ExecutionStatistics getRequestStatistics();

    /**
     * Get a {@link ResourceMethod resource method} for which this {@link ResourceMethodStatistics} are calculated.
     *
     * @return Resource method.
     */
    public ResourceMethod getResourceMethod();

    /**
     * Get the immutable and consistent snapshot of the monitoring statistics. Working with snapshots might
     * have negative performance impact as snapshot must be created but ensures consistency of data over time.
     * However, the usage of snapshot is encouraged to avoid working with inconsistent data. Not all statistics
     * must be updated in the same time on mutable version of statistics.
     *
     * @return Snapshot of resource method statistics.
     * @deprecated implementing class is immutable hence snapshot creation is not needed anymore
     */
    @Deprecated
    public ResourceMethodStatistics snapshot();
}
