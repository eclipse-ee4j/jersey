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

/**
 * Monitoring statistics of {@link javax.ws.rs.ext.ExceptionMapper exception mapper} executions.
 * <p/>
 * Statistics retrieved from Jersey runtime might be mutable and thanks to it might provide inconsistent data
 * as not all statistics are updated in the same time. To retrieve the immutable and consistent
 * statistics data the method {@link #snapshot()} should be used.
 *
 * @author Miroslav Fuksa
 * @see MonitoringStatistics See monitoring statistics for general details about statistics.
 */
public interface ExceptionMapperStatistics {


    /**
     * Get the count of exception mapper executions. The returned map contains {@link Class classes}
     * of {@link javax.ws.rs.ext.ExceptionMapper exception mappers} and corresponding execution count
     * as values. One execution of exception mapper is one call
     * of {@link javax.ws.rs.ext.ExceptionMapper#toResponse(Throwable)} method.
     *
     * @return Map with exception mappers as keys and execution count as values.
     */
    public Map<Class<?>, Long> getExceptionMapperExecutions();

    /**
     * Get count of all successful exception mappings. Successful exception mapping occurs when
     * any {@link javax.ws.rs.ext.ExceptionMapper exception mapper} returns an valid response
     * (even if response contains non-successful response status code).
     *
     * @return Count of successfully mapped exception.
     */
    public long getSuccessfulMappings();

    /**
     * Get count of all unsuccessful exception mappings. Unsuccessful exception mapping occurs when
     * any exception mapping process does not produce an valid response. The reason can be that the
     * {@link javax.ws.rs.ext.ExceptionMapper exception mapper} is not found, or is found but throws
     * exception.
     *
     * @return Count of unmapped exception.
     */
    public long getUnsuccessfulMappings();

    /**
     * Get count of exception mappings that were performed on exceptions.
     *
     * @return Count of all exception being mapped in the runtime.
     */
    public long getTotalMappings();


    /**
     * Get the immutable consistent snapshot of the monitoring statistics. Working with snapshots might
     * have negative performance impact as snapshot must be created but ensures consistency of data over time.
     * However, the usage of snapshot is encouraged to avoid working with inconsistent data. Not all statistics
     * must be updated in the same time on mutable version of statistics.
     *
     * @return Snapshot of exception mapper statistics.
     * @deprecated implementing class is immutable hence snapshot creation is not needed anymore
     */
    @Deprecated
    public ExceptionMapperStatistics snapshot();
}
