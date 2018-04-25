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

package org.glassfish.jersey.server.internal.monitoring.jmx;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.server.monitoring.ExceptionMapperMXBean;
import org.glassfish.jersey.server.monitoring.ExceptionMapperStatistics;

/**
 * MXBean implementing a {@link org.glassfish.jersey.server.monitoring.ExceptionMapperMXBean} mxbean interface.
 *
 * @author Miroslav Fuksa
 */
public class ExceptionMapperMXBeanImpl implements ExceptionMapperMXBean {
    private volatile ExceptionMapperStatistics mapperStatistics;
    private volatile Map<String, Long> mapperExcecutions = new HashMap<>();

    /**
     * Create a new MXBean and register it into mbean server using {@code mBeanExposer}.
     *
     * @param mapperStatistics Exception mapper statistics that should be exposed.
     * @param mBeanExposer Mbean exposer.
     * @param parentName Object name prefix of the parent mbeans.
     */
    public ExceptionMapperMXBeanImpl(ExceptionMapperStatistics mapperStatistics,
                                     MBeanExposer mBeanExposer, String parentName) {
        mBeanExposer.registerMBean(this, parentName + ",exceptions=ExceptionMapper");
        updateExceptionMapperStatistics(mapperStatistics);
    }

    /**
     * Update the MXBean with new statistics.
     *
     * @param mapperStatistics New exception mapper statistics.
     */
    public void updateExceptionMapperStatistics(ExceptionMapperStatistics mapperStatistics) {
        this.mapperStatistics = mapperStatistics;

        for (Map.Entry<Class<?>, Long> entry : mapperStatistics.getExceptionMapperExecutions().entrySet()) {
            mapperExcecutions.put(entry.getKey().getName(), entry.getValue());
        }
    }

    @Override
    public Map<String, Long> getExceptionMapperCount() {
        return mapperExcecutions;
    }

    @Override
    public long getSuccessfulMappings() {
        return mapperStatistics.getSuccessfulMappings();
    }

    @Override
    public long getUnsuccessfulMappings() {
        return mapperStatistics.getUnsuccessfulMappings();
    }

    @Override
    public long getTotalMappings() {
        return mapperStatistics.getTotalMappings();
    }


}
