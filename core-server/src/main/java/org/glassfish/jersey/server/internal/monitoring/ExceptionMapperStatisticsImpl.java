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

package org.glassfish.jersey.server.internal.monitoring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.jersey.server.monitoring.ExceptionMapperStatistics;

/**
 * Exception mapper statistics.
 *
 * @author Miroslav Fuksa
 */
final class ExceptionMapperStatisticsImpl implements ExceptionMapperStatistics {

    /**
     * Builder of exception mapper statistics.
     * <p/>
     * This builder does not need to be threadsafe since it's called only from the jersey-background-task-scheduler.
     */
    static class Builder {

        private Map<Class<?>, Long> exceptionMapperExecutionCountMap = new HashMap<>();
        private long successfulMappings;
        private long unsuccessfulMappings;
        private long totalMappings;

        private ExceptionMapperStatisticsImpl cached;

        /**
         * Add mappings.
         *
         * @param success True if mappings were successful.
         * @param count Number of mappings.
         */
        void addMapping(final boolean success, final int count) {
            cached = null;

            totalMappings++;
            if (success) {
                successfulMappings += count;
            } else {
                unsuccessfulMappings += count;
            }
        }

        /**
         * Add an execution of exception mapper.
         *
         * @param mapper Exception mapper.
         * @param count Number of executions of the {@code mapper}.
         */
        void addExceptionMapperExecution(final Class<?> mapper, final int count) {
            cached = null;

            Long cnt = exceptionMapperExecutionCountMap.get(mapper);
            cnt = cnt == null ? count : cnt + count;
            exceptionMapperExecutionCountMap.put(mapper, cnt);
        }

        /**
         * Build an instance of exception mapper statistics.
         *
         * @return New instance of exception mapper statistics.
         */
        public ExceptionMapperStatisticsImpl build() {
            if (cached == null) {
                cached = new ExceptionMapperStatisticsImpl(new HashMap<>(this.exceptionMapperExecutionCountMap),
                        successfulMappings, unsuccessfulMappings, totalMappings);
            }

            return cached;
        }
    }

    private final Map<Class<?>, Long> exceptionMapperExecutionCount;
    private final long successfulMappings;
    private final long unsuccessfulMappings;
    private final long totalMappings;

    private ExceptionMapperStatisticsImpl(final Map<Class<?>, Long> exceptionMapperExecutionCount, final long successfulMappings,
                                          final long unsuccessfulMappings, final long totalMappings) {
        this.exceptionMapperExecutionCount = Collections.unmodifiableMap(exceptionMapperExecutionCount);
        this.successfulMappings = successfulMappings;
        this.unsuccessfulMappings = unsuccessfulMappings;
        this.totalMappings = totalMappings;
    }

    @Override
    public Map<Class<?>, Long> getExceptionMapperExecutions() {
        return exceptionMapperExecutionCount;
    }

    @Override
    public long getSuccessfulMappings() {
        return successfulMappings;
    }

    @Override
    public long getUnsuccessfulMappings() {
        return unsuccessfulMappings;
    }

    @Override
    public long getTotalMappings() {
        return totalMappings;
    }

    @Override
    public ExceptionMapperStatistics snapshot() {
        // snapshot functionality not yet implemented
        return this;
    }

}
