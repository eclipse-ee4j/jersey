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

import org.glassfish.jersey.server.monitoring.ResourceStatistics;

/**
 * Group of resource MXBeans.
 *
 * @author Miroslav Fuksa
 */
public class ResourcesMBeanGroup {
    private final Map<String, ResourceMxBeanImpl> exposedResourceMBeans = new HashMap<>();
    private final String parentName;
    private final boolean uriResource;
    private final MBeanExposer exposer;

    /**
     * Create a new group of MXBeans and expose nested Resource MXBeans.
     * @param resourceStatistics Resource statistics that should be exposed by nested MXBeans.
     * @param uriResource {@code true} if the resources are identified by URI (and not by java class names for example).
     * @param mBeanExposer mbean exposer.
     * @param parentName Name of the parent bean.
     */
    public ResourcesMBeanGroup(Map<String, ResourceStatistics> resourceStatistics,
                               boolean uriResource,
                               MBeanExposer mBeanExposer,
                               String parentName) {
        this.uriResource = uriResource;
        this.exposer = mBeanExposer;
        this.parentName = parentName;

        updateResourcesStatistics(resourceStatistics);
    }

    /**
     * Update the resource statistics exposed by nested resource beans.
     *
     * @param resourceStatistics New resource statistics.
     */
    public void updateResourcesStatistics(Map<String, ResourceStatistics> resourceStatistics) {
        for (Map.Entry<String, ResourceStatistics> entry : resourceStatistics.entrySet()) {
            ResourceMxBeanImpl resourceMxBean = exposedResourceMBeans.get(entry.getKey());
            if (resourceMxBean == null) {
                resourceMxBean = new ResourceMxBeanImpl(entry.getValue(), entry.getKey(), uriResource, exposer, parentName);
                exposedResourceMBeans.put(entry.getKey(), resourceMxBean);
            }
            resourceMxBean.updateResourceStatistics(entry.getValue());
        }
    }
}
