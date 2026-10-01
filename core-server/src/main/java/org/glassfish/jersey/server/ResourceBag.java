/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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
 * @author Marek Potociar
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
         * Resource models in the order of their registration.
         */
        private final List<Registration> registrations = new LinkedList<>();

        /**
         * Register a new resource model created from a specific resource class.
         *
         * @param resourceClass introspected resource class.
         * @param resourceModel resource model for the class.
         */
        void registerResource(Class<?> resourceClass, Resource resourceModel) {
            registrations.add(new Registration(resourceClass, resourceModel));
            classes.add(resourceClass);
        }

        /**
         * Register a new resource model created from a specific resource instance.
         *
         * @param resourceInstance introspected resource instance.
         * @param resourceModel    resource model for the instance.
         */
        void registerResource(Object resourceInstance, Resource resourceModel) {
            registrations.add(new Registration(null, resourceModel));
            instances.add(resourceInstance);
        }

        /**
         * Register a new programmatically created resource model.
         *
         * @param resourceModel programmatically created resource model.
         */
        void registerProgrammaticResource(Resource resourceModel) {
            registrations.add(new Registration(null, resourceModel));
            classes.addAll(resourceModel.getHandlerClasses());
            instances.addAll(resourceModel.getHandlerInstances());
        }

        /**
         * Build a resource bag.
         *
         * @return new resource bag initialized with the content of the resource bag builder.
         */
        ResourceBag build() {
            return new ResourceBag(classes, instances, registrations);
        }
    }

    /**
     * Resource model with the resource class it was introspected from.
     *
     * @param resourceClass introspected resource class, {@code null} for models of instances and programmatic models.
     * @param model         resource model.
     */
    private record Registration(Class<?> resourceClass, Resource model) {
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
    /**
     * Registered resource models, before merging models with the same path.
     */
    private final List<Registration> registrations;

    private ResourceBag(Set<Class<?>> classes, Set<Object> instances, List<Registration> registrations) {
        this.classes = classes;
        this.instances = instances;
        this.registrations = registrations;
        this.models = mergeModels(registrations);
    }

    private static List<Resource> mergeModels(List<Registration> registrations) {
        final List<Resource> models = new LinkedList<>();
        final Map<String, Resource> rootResourceMap = new HashMap<>();
        for (Registration registration : registrations) {
            final Resource resourceModel = registration.model();
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
        models.addAll(rootResourceMap.values());
        return models;
    }

    /**
     * Returns a resource bag without the given resource classes and without the resource models
     * introspected from them.
     *
     * @param resourceClasses resource classes to remove.
     * @return new resource bag, or this one if there is nothing to remove.
     */
    ResourceBag withoutResourceClasses(Set<Class<?>> resourceClasses) {
        if (resourceClasses.isEmpty()) {
            return this;
        }
        final Set<Class<?>> remainingClasses = Collections.newSetFromMap(new IdentityHashMap<>());
        remainingClasses.addAll(classes);
        remainingClasses.removeAll(resourceClasses);
        final List<Registration> remainingRegistrations = new LinkedList<>();
        for (Registration registration : registrations) {
            if (registration.resourceClass() == null || !resourceClasses.contains(registration.resourceClass())) {
                remainingRegistrations.add(registration);
            }
        }
        return new ResourceBag(remainingClasses, instances, remainingRegistrations);
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
