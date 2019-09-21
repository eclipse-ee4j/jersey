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
 * Monitoring statistics of responses produced by application.
 * <p/>
 * Statistics retrieved from Jersey runtime might be mutable and thanks to it might provide inconsistent data
 * as not all statistics are updated in the same time. To retrieve the immutable and consistent
 * statistics data the method {@link #snapshot()} should be used.
 * <p/>
 *
 * @author Miroslav Fuksa
 * @see MonitoringStatistics See monitoring statistics for general details about statistics.
 */
public interface ResponseStatistics {

    /**
     * Returns the response code of a last response written by the application.
     *
     * @return Last response status code.
     */
    public Integer getLastResponseCode();

    /**
     * Returns statistics of response codes produces by the application. Keys of a returned
     * map are response status codes and values is the the count of responses with these
     * status count. Values are measured since start of the application.
     *
     * @return Map with status codes keys and count as values.
     */
    public Map<Integer, Long> getResponseCodes();


    /**
     * Get the immutable and consistent snapshot of the monitoring statistics. Working with snapshots might
     * have negative performance impact as snapshot must be created but ensures consistency of data over time.
     * However, the usage of snapshot is encouraged to avoid working with inconsistent data. Not all statistics
     * must be updated in the same time on mutable version of statistics.
     *
     * @return Snapshot of response statistics.
     * @deprecated implementing class is immutable hence snapshot creation is not needed anymore
     */
    @Deprecated
    public ResponseStatistics snapshot();
}
