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

import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ResourceMethodMXBean;
import org.glassfish.jersey.server.monitoring.ResourceMethodStatistics;

/**
 * MXBean implementing the {@link org.glassfish.jersey.server.monitoring.ResourceMethodMXBean} MXBean interface.
 * @author Miroslav Fuksa
 */
public class ResourceMethodMXBeanImpl implements ResourceMethodMXBean {
    private volatile ExecutionStatisticsDynamicBean methodExecutionStatisticsMxBean;
    private volatile ExecutionStatisticsDynamicBean requestExecutionStatisticsMxBean;
    private final String path;
    private final String name;
    private final ResourceMethod resourceMethod;
    private final String methodBeanName;

    /**
     * Create a new MXBean and expose it into mbean server using {@code mBeanExposer}.
     *
     * @param methodStatistics Statistics to be exposed by the MXBean.
     * @param uriResource {@code true} if the enclosing resource is identified by URI (and not by java
     *                                class name for example).
     * @param mBeanExposer MBean exposer.
     * @param parentName Name of the parent bean.
     * @param methodUniqueId method unique identifier in the enclosing resource
     */
    public ResourceMethodMXBeanImpl(ResourceMethodStatistics methodStatistics, boolean uriResource,
                                    MBeanExposer mBeanExposer, String parentName, String methodUniqueId) {

        // init mbean name
        this.resourceMethod = methodStatistics.getResourceMethod();
        final Class<?> handlerClass = resourceMethod.getInvocable().getHandler().getHandlerClass();
        final Class<?>[] paramTypes = resourceMethod.getInvocable().getHandlingMethod().getParameterTypes();
        this.name = resourceMethod.getInvocable().getHandlingMethod().getName();
        StringBuilder params = new StringBuilder();
        for (Class<?> type : paramTypes) {
            params.append(type.getSimpleName()).append(";");
        }
        if (params.length() > 0) {
            params.setLength(params.length() - 1);
        }

        if (uriResource) {
            path = "N/A";
        } else {
            path = resourceMethod.getParent().getParent() == null ? "" : resourceMethod.getParent().getPath();
        }

        final String hash = Integer.toHexString(methodUniqueId.hashCode());

        String beanName = resourceMethod.getHttpMethod() + "->";
        if (uriResource) {
            beanName += handlerClass.getSimpleName()
                    + "." + name + "(" + params.toString() + ")#" + hash;
        } else {
            beanName += name + "(" + params.toString() + ")#"
                    + hash;
        }
        this.methodBeanName = parentName + ",detail=methods,method=" + beanName;


        // register mbean
        mBeanExposer.registerMBean(this, methodBeanName);
        methodExecutionStatisticsMxBean = new ExecutionStatisticsDynamicBean(methodStatistics.getMethodStatistics(),
                mBeanExposer, methodBeanName, MBeanExposer.PROPERTY_EXECUTION_TIMES_METHODS);
        requestExecutionStatisticsMxBean = new ExecutionStatisticsDynamicBean(methodStatistics.getRequestStatistics(),
                mBeanExposer, methodBeanName, MBeanExposer.PROPERTY_EXECUTION_TIMES_REQUESTS);
    }

    /**
     * Update the statistics that are exposed by this MXBean.
     * @param resourceMethodStatisticsImpl New statistics.
     */
    public void updateResourceMethodStatistics(ResourceMethodStatistics resourceMethodStatisticsImpl) {
        this.methodExecutionStatisticsMxBean.updateExecutionStatistics(resourceMethodStatisticsImpl.getMethodStatistics());
        this.requestExecutionStatisticsMxBean.updateExecutionStatistics(resourceMethodStatisticsImpl.getRequestStatistics());
    }


    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getHttpMethod() {
        return resourceMethod.getHttpMethod();
    }

    @Override
    public String getDeclaringClassName() {
        return this.resourceMethod.getInvocable().getHandlingMethod().getDeclaringClass().getName();
    }

    @Override
    public String getConsumesMediaType() {
        return MediaTypes.convertToString(resourceMethod.getConsumedTypes());
    }

    @Override
    public String getProducesMediaType() {
        return MediaTypes.convertToString(resourceMethod.getProducedTypes());
    }

    @Override
    public String getMethodName() {
        return name;
    }
}
