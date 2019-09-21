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
 * MXBean interface of the {@link javax.ws.rs.ext.ExceptionMapper exception mapper} statistics.
 *
 * @author Miroslav Fuksa
 */
public interface ExceptionMapperMXBean {

    /**
     * Get the statistics of execution of exception mappers.
     *
     * @return Map where keys are string class names of {@link javax.ws.rs.ext.ExceptionMapper exception mappers}
     *         and values are counts of execution of these mappers.
     */
    public Map<String, Long> getExceptionMapperCount();

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


}
