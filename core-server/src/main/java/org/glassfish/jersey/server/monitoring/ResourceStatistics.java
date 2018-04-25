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

import java.util.Map;

import org.glassfish.jersey.server.model.ResourceMethod;

/**
 * Monitoring statistics of the resource. The resource is a set of resource methods with specific characteristics
 * that is not defined by this interface. The resource can be set of resource methods that are accessible on
 * the same URI (in the case of {@link org.glassfish.jersey.server.monitoring.MonitoringStatistics#getUriStatistics()})
 * or the set of resource methods defined in one {@link Class} (in case
 * of {@link org.glassfish.jersey.server.monitoring.MonitoringStatistics#getResourceClassStatistics()}).
 * <p/>
 * Statistics retrieved from Jersey runtime might be mutable and thanks to it might provide inconsistent data
 * as not all statistics are updated in the same time. To retrieve the immutable and consistent
 * statistics data the method {@link #snapshot()} should be used.
 * <p/>
 *
 * The principles of using statistics
 * is similar to principles of using {@link MonitoringStatistics}.
 *
 * @author Miroslav Fuksa
 * @see MonitoringStatistics See monitoring statistics for general details about statistics.
 */
public interface ResourceStatistics {
    /**
     * Get {@link ExecutionStatistics execution statistics} that contain measurements of times only for
     * execution of resource methods. Durations average time, minimum time and maximum time
     * measure only time of execution of resource methods code. It does not involve other request processing
     * phases.
     *
     * @return Execution statistics of all resource method in this resource.
     */
    public ExecutionStatistics getResourceMethodExecutionStatistics();

    /**
     * Get {@link ExecutionStatistics execution statistics} that contain measurements of times for
     * whole processing from time when request comes into the Jersey application until the response
     * is written to the underlying IO container. The statistics involves only requests that were matched
     * to resource methods defined in {@link #getResourceMethodStatistics()}.
     *
     * @return Execution statistics of entire request processing for all resource method from this resource.
     */
    public ExecutionStatistics getRequestExecutionStatistics();

    /**
     * Return the statistics for resource method. Keys of returned map are {@link ResourceMethod resource methods}
     * available in the resource and values are execution statistics of these resource methods.
     *
     * @return Map with {@link ResourceMethod resource method} keys
     *         and corresponding {@link ResourceMethodStatistics resource method statistics}.
     */
    public Map<ResourceMethod, ResourceMethodStatistics> getResourceMethodStatistics();

    /**
     * Get the immutable and consistent snapshot of the monitoring statistics. Working with snapshots might
     * have negative performance impact as snapshot must be created but ensures consistency of data over time.
     * However, the usage of snapshot is encouraged to avoid working with inconsistent data. Not all statistics
     * must be updated in the same time on mutable version of statistics.
     *
     * @return Snapshot of resource statistics.
     * @deprecated implementing class is immutable hence snapshot creation is not needed anymore
     */
    @Deprecated
    public ResourceStatistics snapshot();

}
