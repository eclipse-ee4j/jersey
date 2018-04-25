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

import org.glassfish.jersey.server.internal.monitoring.MonitoringUtils;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ResourceMXBean;
import org.glassfish.jersey.server.monitoring.ResourceMethodStatistics;
import org.glassfish.jersey.server.monitoring.ResourceStatistics;

/**
 * MXBean implementing the {@link org.glassfish.jersey.server.monitoring.ResourceMethodMXBean} MXBean interface.
 *
 * @author Miroslav Fuksa
 */
public class ResourceMxBeanImpl implements ResourceMXBean {
    private final String name;
    private volatile ExecutionStatisticsDynamicBean methodsExecutionStatisticsBean;
    private volatile ExecutionStatisticsDynamicBean requestExecutionStatisticsBean;
    private final Map<String, ResourceMethodMXBeanImpl> resourceMethods = new HashMap<>();
    private final String resourcePropertyName;
    private final boolean uriResource;
    private final MBeanExposer mBeanExposer;

    /**
     * Create and register new MXBean into the mbean server using the {@code mBeanExposer}.
     *
     * @param resourceStatistics resource statistics that should be exposed by this and nested MXBeans.
     * @param name Name of the resource.
     * @param uriResource {@code true} if the resource is identified by URI (and not by java class name for example).
     * @param mBeanExposer MBean exposer.
     * @param parentName Name of the parent bean.
     */
    public ResourceMxBeanImpl(ResourceStatistics resourceStatistics, String name, boolean uriResource,
                              MBeanExposer mBeanExposer,
                              String parentName) {
        this.name = name;
        this.uriResource = uriResource;
        this.mBeanExposer = mBeanExposer;
        this.resourcePropertyName = parentName + ",resource=" + MBeanExposer.convertToObjectName(name, uriResource);
        mBeanExposer.registerMBean(this, resourcePropertyName);
        this.methodsExecutionStatisticsBean = new ExecutionStatisticsDynamicBean(
                resourceStatistics.getResourceMethodExecutionStatistics(), mBeanExposer, resourcePropertyName,
                MBeanExposer.PROPERTY_EXECUTION_TIMES_METHODS);
        this.requestExecutionStatisticsBean = new ExecutionStatisticsDynamicBean(
                resourceStatistics.getRequestExecutionStatistics(), mBeanExposer, resourcePropertyName,
                MBeanExposer.PROPERTY_EXECUTION_TIMES_REQUESTS);

        updateResourceStatistics(resourceStatistics);
    }

    /**
     * Update the statistics of this MXBean and of nested MXBeans.
     * @param resourceStatistics New resource statistics.
     */
    public void updateResourceStatistics(ResourceStatistics resourceStatistics) {
        this.methodsExecutionStatisticsBean.updateExecutionStatistics(resourceStatistics.getResourceMethodExecutionStatistics());
        this.requestExecutionStatisticsBean.updateExecutionStatistics(resourceStatistics.getRequestExecutionStatistics());

        for (Map.Entry<ResourceMethod, ResourceMethodStatistics> entry
                : resourceStatistics.getResourceMethodStatistics().entrySet()) {
            final ResourceMethodStatistics methodStats = entry.getValue();
            final ResourceMethod method = entry.getKey();

            final String methodId = MonitoringUtils.getMethodUniqueId(method);

            ResourceMethodMXBeanImpl methodMXBean = this.resourceMethods.get(methodId);
            if (methodMXBean == null) {
                methodMXBean = new ResourceMethodMXBeanImpl(methodStats, uriResource, mBeanExposer,
                        resourcePropertyName, methodId);
                resourceMethods.put(methodId, methodMXBean);
            }
            methodMXBean.updateResourceMethodStatistics(methodStats);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }


}
