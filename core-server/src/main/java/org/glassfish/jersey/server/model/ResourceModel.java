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

package org.glassfish.jersey.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

/**
 * Resource model of the deployed application which contains set of root resources. As it implements {@link
 * ResourceModelComponent} it can be validated by {@link ComponentModelValidator component model validator} which will perform
 * validation of the entire resource model including all sub components ({@link Resource resources},
 * {@link ResourceMethod resource methods} ...).
 *
 * @author Miroslav Fuksa
 */
public class ResourceModel implements ResourceModelComponent {

    /**
     * Builder used to create {@link ResourceModel resource model} instances.
     */
    public static class Builder {
        private final List<Resource> resources;
        private final boolean subResourceModel;


        /**
         * Create new builder pre initialized with {@code resourceModel}.
         *
         * @param resourceModel    Resource model.
         * @param subResourceModel {@code true} if resource model created by this builder will be sub resource model,
         *                         {@code false} if it is a application root resource model.
         */
        public Builder(ResourceModel resourceModel, boolean subResourceModel) {
            this.resources = resourceModel.getResources();
            this.subResourceModel = subResourceModel;
        }

        /**
         * Create new builder pre initialized with {@code resource}.
         *
         * @param resources        Resources (root and non root).
         * @param subResourceModel {@code true} if resource model created by this builder will be sub resource model,
         *                         {@code false} if it is a application root resource model.
         */
        public Builder(List<Resource> resources, boolean subResourceModel) {
            this.resources = resources;
            this.subResourceModel = subResourceModel;
        }

        /**
         * Create new builder with empty resources.
         *
         * @param subResourceModel {@code true} if resource model created by this builder will be sub resource model,
         *                         {@code false} if it is a application root resource model.
         */
        public Builder(boolean subResourceModel) {
            this.resources = new ArrayList<>();
            this.subResourceModel = subResourceModel;
        }


        /**
         * Add a resource to the builder.
         *
         * @param resource Resource to be added to the builder (root or non root resource).
         * @return Current builder.
         */
        public Builder addResource(Resource resource) {
            this.resources.add(resource);
            return this;
        }

        /**
         * Build the {@link ResourceModel resource model}. Resources with the same path are merged.
         *
         * @return Resource model.
         */
        public ResourceModel build() {
            Map<String, Resource> resourceMap = new LinkedHashMap<>();
            // resource with no path that should not be merged
            final Set<Resource> separateResources = Collections.newSetFromMap(new IdentityHashMap<>());

            for (Resource resource : resources) {
                final String path = resource.getPath();
                if (path == null && !subResourceModel) {
                    separateResources.add(resource);
                } else {
                    final Resource fromMap = resourceMap.get(path);
                    if (fromMap == null) {
                        resourceMap.put(path, resource);
                    } else {
                        resourceMap.put(path, Resource.builder(fromMap).mergeWith(resource).build());
                    }
                }
            }
            List<Resource> rootResources = new ArrayList<>();
            List<Resource> allResources = new ArrayList<>();

            for (Map.Entry<String, Resource> entry : resourceMap.entrySet()) {
                if (entry.getKey() != null) {
                    rootResources.add(entry.getValue());
                }
                allResources.add(entry.getValue());
            }
            if (!subResourceModel) {
                allResources.addAll(separateResources);
            }

            return new ResourceModel(rootResources, allResources);
        }
    }

    private final List<Resource> rootResources;
    private final List<Resource> resources;

    private final Value<RuntimeResourceModel> runtimeRootResourceModelValue;

    /**
     * Creates new instance from root allResources.
     *
     * @param allResources Root resource of the resource model.
     */
    private ResourceModel(List<Resource> rootResources, List<Resource> allResources) {
        this.resources = allResources;
        this.rootResources = rootResources;
        this.runtimeRootResourceModelValue = Values.lazy(new Value<RuntimeResourceModel>() {
            @Override
            public RuntimeResourceModel get() {
                return new RuntimeResourceModel(ResourceModel.this.resources);
            }
        });
    }


    /**
     * Return root resources from this resource model.
     *
     * @return List of root resources.
     */
    public List<Resource> getRootResources() {
        return rootResources;
    }

    /**
     * Return all resources from this resource model.
     *
     * @return List of all resources (root and non root resources).
     */
    public List<Resource> getResources() {
        return resources;
    }

    @Override
    public void accept(ResourceModelVisitor visitor) {
        visitor.visitResourceModel(this);
    }

    @Override
    public List<? extends ResourceModelComponent> getComponents() {
        List<ResourceModelComponent> components = new ArrayList<>();

        components.addAll(resources);
        components.addAll(getRuntimeResourceModel().getRuntimeResources());
        return components;
    }

    /**
     * Return {@link RuntimeResourceModel runtime resource model} based on this this resource model.
     *
     * @return Runtime resource model created from this resource model.
     */
    public RuntimeResourceModel getRuntimeResourceModel() {
        return runtimeRootResourceModelValue.get();
    }
}
