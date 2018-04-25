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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import org.glassfish.jersey.internal.util.collection.Views;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ExceptionMapperStatistics;
import org.glassfish.jersey.server.monitoring.ExecutionStatistics;
import org.glassfish.jersey.server.monitoring.MonitoringStatistics;
import org.glassfish.jersey.server.monitoring.ResourceStatistics;
import org.glassfish.jersey.server.monitoring.ResponseStatistics;

/**
 * Monitoring statistics implementation.
 * <p/>
 * This object is loosely immutable (i.e., {@link #getResourceClassStatistics()} and {@link #getUriStatistics()} gets updated on
 * access). As a result, it is unnecessary to call {@link #snapshot()}.
 *
 * @author Miroslav Fuksa
 */
final class MonitoringStatisticsImpl implements MonitoringStatistics {

    /**
     * Builder of monitoring statistics.
     * <p/>
     * This builder does not need to be threadsafe as it's only accessed by jersey-background-task-scheduler. However, {@link
     * #BUILDING_FUNCTION} is triggered when it is accessed (e.g., by servlet-container thread-pool threads) which adds threadsafe
     * constraint on some of the sub-builders.
     * <p/>
     * Sub-Builders that require thread-safety
     * <pre><ul>
     *     <li>{@link org.glassfish.jersey.server.internal.monitoring.ExecutionStatisticsImpl.Builder}</li>
     *     <li>{@link org.glassfish.jersey.server.internal.monitoring.ResourceStatisticsImpl.Builder}</li>
     *     <li>{@link org.glassfish.jersey.server.internal.monitoring.ResourceMethodStatisticsImpl.Builder}</li>
     *     <li>{@link org.glassfish.jersey.server.internal.monitoring.TimeWindowStatisticsImpl.Builder}</li>
     * </ul>
     * The rest does not need to be thread-safe
     * <ul>
     *     <li>{@link org.glassfish.jersey.server.internal.monitoring.ExceptionMapperStatisticsImpl.Builder}</li>
     *     <li>{@link org.glassfish.jersey.server.internal.monitoring.ResponseStatisticsImpl.Builder}</li>
     * </ul></pre>
     */
    static class Builder {

        private static final Function<ResourceStatisticsImpl.Builder, ResourceStatistics> BUILDING_FUNCTION =
                ResourceStatisticsImpl.Builder::build;

        private final ResponseStatisticsImpl.Builder responseStatisticsBuilder;
        private final ExceptionMapperStatisticsImpl.Builder exceptionMapperStatisticsBuilder;

        private final ResourceMethodStatisticsImpl.Factory methodFactory = new ResourceMethodStatisticsImpl.Factory();
        private final SortedMap<String, ResourceStatisticsImpl.Builder> uriStatistics = new TreeMap<>();
        private final SortedMap<Class<?>, ResourceStatisticsImpl.Builder> resourceClassStatistics
                = new TreeMap<>((o1, o2) -> o1.getName().compareTo(o2.getName()));

        private ExecutionStatisticsImpl.Builder executionStatisticsBuilder;

        /**
         * Create a new builder.
         */
        Builder() {
            this.responseStatisticsBuilder = new ResponseStatisticsImpl.Builder();
            this.exceptionMapperStatisticsBuilder = new ExceptionMapperStatisticsImpl.Builder();
        }

        /**
         * Create a new builder and initialize it from resource model.
         *
         * @param resourceModel resource model.
         */
        Builder(final ResourceModel resourceModel) {
            this();

            for (final Resource resource : resourceModel.getRootResources()) {
                processResource(resource, "");
                for (final Resource child : resource.getChildResources()) {
                    final String path = resource.getPath();
                    processResource(child, path.startsWith("/") ? path : "/" + path);
                }
            }

        }

        private void processResource(final Resource resource, final String pathPrefix) {
            final StringBuilder pathSB = new StringBuilder(pathPrefix);
            if (!pathPrefix.endsWith("/") && !resource.getPath().startsWith("/")) {
                pathSB.append("/");
            }
            pathSB.append(resource.getPath());

            uriStatistics.put(pathSB.toString(), new ResourceStatisticsImpl.Builder(resource, methodFactory));

            for (final ResourceMethod resourceMethod : resource.getResourceMethods()) {
                getOrCreateResourceBuilder(resourceMethod).addMethod(resourceMethod);
            }
        }

        private ResourceStatisticsImpl.Builder getOrCreateResourceBuilder(final ResourceMethod resourceMethod) {
            final Class<?> clazz = resourceMethod.getInvocable().getHandler().getHandlerClass();
            ResourceStatisticsImpl.Builder builder = resourceClassStatistics.get(clazz);
            if (builder == null) {
                builder = new ResourceStatisticsImpl.Builder(methodFactory);
                resourceClassStatistics.put(clazz, builder);
            }
            return builder;
        }

        /**
         * Get the exception mapper statistics builder.
         *
         * @return Builder of internal exception mapper statistics.
         */
        ExceptionMapperStatisticsImpl.Builder getExceptionMapperStatisticsBuilder() {
            return exceptionMapperStatisticsBuilder;
        }

        /**
         * Add global request execution.
         *
         * @param startTime time of the execution.
         * @param duration  duration of the execution.
         */
        void addRequestExecution(final long startTime, final long duration) {
            if (executionStatisticsBuilder == null) {
                executionStatisticsBuilder = new ExecutionStatisticsImpl.Builder();
            }
            executionStatisticsBuilder.addExecution(startTime, duration);
        }

        /**
         * Add execution of a resource method.
         *
         * @param uri             String uri which was executed.
         * @param resourceMethod  Resource method.
         * @param methodTime      Time spent on execution of resource method itself (Unix timestamp format).
         * @param methodDuration  Time of execution of the resource method.
         * @param requestTime     Time of whole request processing (from receiving the request until writing the response). (Unix
         *                        timestamp format)
         * @param requestDuration Time when the request matching to the executed resource method has been received by Jersey.
         */
        void addExecution(final String uri, final ResourceMethod resourceMethod,
                          final long methodTime, final long methodDuration,
                          final long requestTime, final long requestDuration) {
            // Uri resource stats.
            ResourceStatisticsImpl.Builder uriStatsBuilder = uriStatistics.get(uri);
            if (uriStatsBuilder == null) {
                uriStatsBuilder = new ResourceStatisticsImpl.Builder(resourceMethod.getParent(), methodFactory);
                uriStatistics.put(uri, uriStatsBuilder);
            }
            uriStatsBuilder.addExecution(resourceMethod, methodTime, methodDuration, requestTime, requestDuration);

            // Class resource stats.
            final ResourceStatisticsImpl.Builder classStatsBuilder = getOrCreateResourceBuilder(resourceMethod);
            classStatsBuilder.addExecution(resourceMethod, methodTime, methodDuration, requestTime, requestDuration);

            // Resource method stats.
            methodFactory.getOrCreate(resourceMethod)
                         .addResourceMethodExecution(methodTime, methodDuration, requestTime, requestDuration);
        }

        /**
         * Add a response status code produces by Jersey.
         *
         * @param responseCode Response status code.
         */
        void addResponseCode(final int responseCode) {
            responseStatisticsBuilder.addResponseCode(responseCode);
        }

        /**
         * Build a new instance of monitoring statistics.
         *
         * @return New instance of {@code MonitoringStatisticsImpl}.
         */
        MonitoringStatisticsImpl build() {
            final Map<String, ResourceStatistics> uriStats = Collections.unmodifiableMap(
                    Views.mapView(uriStatistics, BUILDING_FUNCTION));
            final Map<Class<?>, ResourceStatistics> classStats = Collections.unmodifiableMap(
                    Views.mapView(resourceClassStatistics, BUILDING_FUNCTION));

            final ExecutionStatistics requestStats = executionStatisticsBuilder == null
                    ? ExecutionStatisticsImpl.EMPTY : executionStatisticsBuilder.build();

            return new MonitoringStatisticsImpl(
                    uriStats, classStats, requestStats,
                    responseStatisticsBuilder.build(),
                    exceptionMapperStatisticsBuilder.build());
        }
    }

    private final ExecutionStatistics requestStatistics;
    private final ResponseStatistics responseStatistics;
    private final ExceptionMapperStatistics exceptionMapperStatistics;
    private final Map<String, ResourceStatistics> uriStatistics;
    private final Map<Class<?>, ResourceStatistics> resourceClassStatistics;

    private MonitoringStatisticsImpl(final Map<String, ResourceStatistics> uriStatistics,
                                     final Map<Class<?>, ResourceStatistics> resourceClassStatistics,
                                     final ExecutionStatistics requestStatistics,
                                     final ResponseStatistics responseStatistics,
                                     final ExceptionMapperStatistics exceptionMapperStatistics) {
        this.uriStatistics = uriStatistics;
        this.resourceClassStatistics = resourceClassStatistics;
        this.requestStatistics = requestStatistics;
        this.responseStatistics = responseStatistics;
        this.exceptionMapperStatistics = exceptionMapperStatistics;
    }

    @Override
    public ExecutionStatistics getRequestStatistics() {
        return requestStatistics;
    }

    @Override
    public ResponseStatistics getResponseStatistics() {
        return responseStatistics;
    }

    /**
     * Refreshed (re-built) on every access.
     *
     * @return resource statistics
     */
    @Override
    public Map<String, ResourceStatistics> getUriStatistics() {
        return uriStatistics;
    }

    /**
     * Refreshed (re-built) on every access.
     *
     * @return resource statistics
     */
    @Override
    public Map<Class<?>, ResourceStatistics> getResourceClassStatistics() {
        return resourceClassStatistics;
    }

    @Override
    public ExceptionMapperStatistics getExceptionMapperStatistics() {
        return exceptionMapperStatistics;
    }

    @Override
    public MonitoringStatistics snapshot() {
        // snapshot is not needed, this object is loosely immutable (see javadoc of Maps getters)
        // all the other Statistics objects are immutable
        return this;
    }
}
