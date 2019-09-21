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

package org.glassfish.jersey.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.uri.PathTemplate;

/**
 * Runtime Resource model contains structured information about runtime resources.
 *
 * @author Miroslav Fuksa
 */
public class RuntimeResourceModel {

    private final List<RuntimeResource> runtimeResources;

    /**
     * Creates new runtime resource model from the list of resources.
     *
     * @param resources List of all resource that should be base for the model.
     */
    public RuntimeResourceModel(List<Resource> resources) {
        this.runtimeResources = new ArrayList<>();
        for (RuntimeResource.Builder builder : getRuntimeResources(resources)) {
            runtimeResources.add(builder.build(null));
        }
        Collections.sort(runtimeResources, RuntimeResource.COMPARATOR);
    }

    private List<RuntimeResource.Builder> getRuntimeResources(List<Resource> resources) {
        Map<String, List<Resource>> regexMap = new HashMap<>();

        for (Resource resource : resources) {
            String path = resource.getPath();
            String regex = null;
            if (path != null) {
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                regex = new PathTemplate(path).getPattern().getRegex();
            }

            List<Resource> listFromMap = regexMap.get(regex);
            if (listFromMap == null) {
                listFromMap = new ArrayList<>();
                regexMap.put(regex, listFromMap);
            }
            listFromMap.add(resource);
        }

        List<RuntimeResource.Builder> runtimeResources = new ArrayList<>();
        for (Map.Entry<String, List<Resource>> entry : regexMap.entrySet()) {
            final List<Resource> resourcesWithSameRegex = entry.getValue();

            List<Resource> childResources = new ArrayList<>();
            for (final Resource res : resourcesWithSameRegex) {
                childResources.addAll(res.getChildResources());
            }

            List<RuntimeResource.Builder> childRuntimeResources = getRuntimeResources(childResources);
            runtimeResources.add(new RuntimeResource.Builder(resourcesWithSameRegex, childRuntimeResources, entry.getKey()));
        }
        return runtimeResources;
    }

    /**
     * Get list of runtime resources from this model (excluding child resources which are accessible in the
     * returned resources).
     *
     * @return List of runtime resources.
     */
    public List<RuntimeResource> getRuntimeResources() {
        return runtimeResources;
    }
}
