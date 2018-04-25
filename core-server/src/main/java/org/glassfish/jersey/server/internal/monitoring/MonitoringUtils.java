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

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ExecutionStatistics;
import org.glassfish.jersey.server.monitoring.TimeWindowStatistics;

/**
 * Monitoring helper class that contains utility methods used in
 * Monitoring.
 *
 * @author Miroslav Fuksa
 */
public final class MonitoringUtils {

    /**
     * Request rate limit (per second) below which statistics can be considered as cacheable.
     */
    private static final double CACHEABLE_REQUEST_RATE_LIMIT = 0.001;

    /**
     * Get the method unique string ID. The ID is constructed from method attributes separated
     * by pipe '|'. The attributes are used in the following order:
     * method-produces|method-consumes|http-method|method-path|method-java-name
     * <p>
     *     If any of the attributes is not defined, "null" is used for such an attribute.
     * <p/>
     *
     * @param method Resource method.
     * @return String constructed from resource method parameters.
     */
    public static String getMethodUniqueId(final ResourceMethod method) {
        final String path = method.getParent() != null ? createPath(method.getParent()) : "null";

        return method.getProducedTypes().toString() + "|"
                + method.getConsumedTypes().toString() + "|"
                + method.getHttpMethod() + "|"
                + path + "|"
                + method.getInvocable().getHandlingMethod().getName();
    }

    private static String createPath(Resource resource) {
        return appendPath(resource, new StringBuilder()).toString();
    }

    private static StringBuilder appendPath(Resource resource, StringBuilder path) {
        return resource.getParent() == null ? path.append(resource.getPath())
                : appendPath(resource.getParent(), path).append(".").append(resource.getPath());
    }

    /**
     * Indicates whether the global, resource, resource method statistics containing the give execution statistics can
     * be cached.
     *
     * @param stats execution statistics to be examined.
     * @return {@code true} if the statistics can be cached, {@code false} otherwise.
     */
    static boolean isCacheable(final ExecutionStatistics stats) {
        for (final TimeWindowStatistics window : stats.getTimeWindowStatistics().values()) {
            if (window.getRequestsPerSecond() >= CACHEABLE_REQUEST_RATE_LIMIT) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prevent instantiation.
     */
    private MonitoringUtils() {
    }
}
