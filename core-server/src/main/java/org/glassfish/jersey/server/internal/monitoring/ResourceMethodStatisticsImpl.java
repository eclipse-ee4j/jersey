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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ExecutionStatistics;
import org.glassfish.jersey.server.monitoring.ResourceMethodStatistics;

/**
 * Immutable resource method statistics.
 *
 * @author Miroslav Fuksa
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
final class ResourceMethodStatisticsImpl implements ResourceMethodStatistics {

    /**
     * Factory for creating and storing resource method statistics. One instance per resource method.
     * <p/>
     * Must be thread-safe.
     */
    static class Factory {

        private final ConcurrentMap<String, Builder> stringToMethodsBuilders = new ConcurrentHashMap<>();

        ResourceMethodStatisticsImpl.Builder getOrCreate(final ResourceMethod resourceMethod) {
            final String methodUniqueId = MonitoringUtils.getMethodUniqueId(resourceMethod);

            if (!stringToMethodsBuilders.containsKey(methodUniqueId)) {
                stringToMethodsBuilders.putIfAbsent(methodUniqueId, new ResourceMethodStatisticsImpl.Builder(resourceMethod));
            }
            return stringToMethodsBuilders.get(methodUniqueId);
        }
    }

    /**
     * Builder of resource method statistics.
     * <p/>
     * Must be thread-safe.
     */
    static class Builder {

        private final ResourceMethod resourceMethod;

        private final AtomicReference<ExecutionStatisticsImpl.Builder> resourceMethodExecutionStatisticsBuilder = new
                AtomicReference<>();
        private final AtomicReference<ExecutionStatisticsImpl.Builder> requestExecutionStatisticsBuilder = new
                AtomicReference<>();

        private volatile ResourceMethodStatisticsImpl cached;

        /**
         * Create a new builder instance.
         *
         * @param resourceMethod Resource method for which statistics are evaluated.
         */
        Builder(final ResourceMethod resourceMethod) {
            this.resourceMethod = resourceMethod;
        }

        /**
         * Build an instance of resource method statistics.
         *
         * @return New instance of resource method statistics.
         */
        ResourceMethodStatisticsImpl build() {
            ResourceMethodStatisticsImpl cachedLocalReference = cached;
            if (cachedLocalReference != null) {
                return cachedLocalReference;
            }

            final ExecutionStatistics methodStats = resourceMethodExecutionStatisticsBuilder.get() == null
                    ? ExecutionStatisticsImpl.EMPTY : resourceMethodExecutionStatisticsBuilder.get().build();
            final ExecutionStatistics requestStats = requestExecutionStatisticsBuilder.get() == null
                    ? ExecutionStatisticsImpl.EMPTY : requestExecutionStatisticsBuilder.get().build();

            final ResourceMethodStatisticsImpl stats = new ResourceMethodStatisticsImpl(resourceMethod, methodStats,
                    requestStats);

            if (MonitoringUtils.isCacheable(methodStats)) {
                // overwrite the cache regardless of whether it's null or not
                cached = stats;
            }

            return stats;
        }

        /**
         * Add execution of the resource method to the statistics.
         *
         * @param methodStartTime  Time spent on execution of resource method itself (Unix timestamp format).
         * @param methodDuration   Time of execution of the resource method.
         * @param requestStartTime Time of whole request processing (from receiving the request until writing the response). (Unix
         *                         timestamp format)
         * @param requestDuration  Time when the request matching to the executed resource method has been received by Jersey.
         */
        void addResourceMethodExecution(final long methodStartTime, final long methodDuration, final long requestStartTime,
                                        final long requestDuration) {
            cached = null;

            if (resourceMethodExecutionStatisticsBuilder.get() == null) {
                resourceMethodExecutionStatisticsBuilder.compareAndSet(null, new ExecutionStatisticsImpl.Builder());
            }
            resourceMethodExecutionStatisticsBuilder.get().addExecution(methodStartTime, methodDuration);

            if (requestExecutionStatisticsBuilder.get() == null) {
                requestExecutionStatisticsBuilder.compareAndSet(null, new ExecutionStatisticsImpl.Builder());
            }
            requestExecutionStatisticsBuilder.get().addExecution(requestStartTime, requestDuration);
        }
    }

    private final ExecutionStatistics resourceMethodExecutionStatistics;
    private final ExecutionStatistics requestExecutionStatistics;
    private final ResourceMethod resourceMethod;

    private ResourceMethodStatisticsImpl(final ResourceMethod resourceMethod,
                                         final ExecutionStatistics resourceMethodExecutionStatistics,
                                         final ExecutionStatistics requestExecutionStatistics) {
        this.resourceMethod = resourceMethod;

        this.resourceMethodExecutionStatistics = resourceMethodExecutionStatistics;
        this.requestExecutionStatistics = requestExecutionStatistics;
    }

    @Override
    public ExecutionStatistics getRequestStatistics() {
        return requestExecutionStatistics;
    }

    @Override
    public ExecutionStatistics getMethodStatistics() {
        return resourceMethodExecutionStatistics;
    }

    public ResourceMethod getResourceMethod() {
        return resourceMethod;
    }

    @Override
    public ResourceMethodStatistics snapshot() {
        // this object is immutable (not considering ResourceMethod which is not a monitoring object)
        return this;
    }
}
