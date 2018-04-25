/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.server.model.Resource;

/**
 * A container for application resource models used during the {@link ApplicationHandler}
 * initialization.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class ResourceBag {
    /**
     * Resource bag builder.
     */
    public static final class Builder {
        /**
         * Resource handler classes for the models in this resource bag.
         */
        private final Set<Class<?>> classes = Collections.newSetFromMap(new IdentityHashMap<>());
        /**
         * Resource handler instance for the models in this resource bag.
         */
        private final Set<Object> instances = Collections.newSetFromMap(new IdentityHashMap<>());
        /**
         * Resource models.
         */
        private final List<Resource> models = new LinkedList<Resource>();
        /**
         * Map of root path to resource model.
         */
        private final Map<String, Resource> rootResourceMap = new HashMap<String, Resource>();

        /**
         * Register a new resource model created from a specific resource class.
         *
         * @param resourceClass introspected resource class.
         * @param resourceModel resource model for the class.
         */
        void registerResource(Class<?> resourceClass, Resource resourceModel) {
            registerModel(resourceModel);
            classes.add(resourceClass);
        }

        /**
         * Register a new resource model created from a specific resource instance.
         *
         * @param resourceInstance introspected resource instance.
         * @param resourceModel    resource model for the instance.
         */
        void registerResource(Object resourceInstance, Resource resourceModel) {
            registerModel(resourceModel);
            instances.add(resourceInstance);
        }

        /**
         * Register a new programmatically created resource model.
         *
         * @param resourceModel programmatically created resource model.
         */
        void registerProgrammaticResource(Resource resourceModel) {
            registerModel(resourceModel);
            classes.addAll(resourceModel.getHandlerClasses());
            instances.addAll(resourceModel.getHandlerInstances());
        }

        private void registerModel(Resource resourceModel) {
            final String path = resourceModel.getPath();
            if (path != null) {
                Resource existing = rootResourceMap.get(path);
                if (existing != null) {
                    // merge resources
                    existing = Resource.builder(existing).mergeWith(resourceModel).build();
                    rootResourceMap.put(path, existing);
                } else {
                    rootResourceMap.put(path, resourceModel);
                }
            } else {
                models.add(resourceModel);
            }
        }

        /**
         * Build a resource bag.
         *
         * @return new resource bag initialized with the content of the resource bag builder.
         */
        ResourceBag build() {
            models.addAll(rootResourceMap.values());
            return new ResourceBag(classes, instances, models);
        }
    }

    /**
     * Resource handler classes for the models in this resource bag.
     */
    final Set<Class<?>> classes;
    /**
     * Resource handler instance for the models in this resource bag.
     */
    final Set<Object> instances;
    /**
     * Resource models.
     */
    final List<Resource> models;

    private ResourceBag(Set<Class<?>> classes, Set<Object> instances, List<Resource> models) {
        this.classes = classes;
        this.instances = instances;
        this.models = models;
    }


    /**
     * Returns list of root resources.
     *
     * @return list of root resources.
     */
    List<Resource> getRootResources() {
        List<Resource> rootResources = new ArrayList<Resource>();
        for (Resource resource : models) {
            if (resource.getPath() != null) {
                rootResources.add(resource);
            }
        }
        return rootResources;
    }
}
