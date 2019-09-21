/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking.mapping;

import java.lang.reflect.Type;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Context;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ExtendedResourceContext;
import org.glassfish.jersey.server.model.HandlerConstructor;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.MethodHandler;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.model.ResourceModelComponent;
import org.glassfish.jersey.server.model.ResourceModelVisitor;
import org.glassfish.jersey.server.model.RuntimeResource;
import org.glassfish.jersey.uri.PathPattern;
import org.glassfish.jersey.uri.UriTemplate;

/**
 * This implementation of the resource mapping context assumed resources are
 * of a simple type with a statically defined structure.
 *
 * @author Gerard Davison (gerard.davison at oracle.com)
 */

public class NaiveResourceMappingContext implements ResourceMappingContext {

    private final ExtendedResourceContext erc;

    private Map<Class<?>, ResourceMappingContext.Mapping> mappings;

    public NaiveResourceMappingContext(@Context final ExtendedResourceContext erc) {
        this.erc = erc;
    }

    @Override
    public Mapping getMapping(final Class<?> resource) {
        buildMappings();
        return mappings.get(resource);
    }

    private void buildMappings() {
        if (mappings != null) {
            return;
        }
        mappings = new HashMap<>();

        erc.getResourceModel().accept(new ResourceModelVisitor() {

            Deque<PathPattern> stack = new LinkedList<>();

            private void processComponents(final ResourceModelComponent component) {

                final List<? extends ResourceModelComponent> components = component.getComponents();
                if (components != null) {
                    for (final ResourceModelComponent rc : components) {
                        rc.accept(this);
                    }
                }
            }

            @Override
            public void visitInvocable(final Invocable invocable) {
                processComponents(invocable);
            }

            @Override
            public void visitRuntimeResource(final RuntimeResource runtimeResource) {
                processComponents(runtimeResource);
            }

            @Override
            public void visitResourceModel(final ResourceModel resourceModel) {
                processComponents(resourceModel);
            }

            @Override
            public void visitResourceHandlerConstructor(final HandlerConstructor handlerConstructor) {
                processComponents(handlerConstructor);
            }

            @Override
            public void visitMethodHandler(final MethodHandler methodHandler) {
                processComponents(methodHandler);
            }

            @Override
            public void visitChildResource(final Resource resource) {
                visitResourceIntl(resource, false);
            }

            @Override
            public void visitResource(final Resource resource) {

                visitResourceIntl(resource, true);
            }

            private void visitResourceIntl(final Resource resource, final boolean isRoot) {
                try {
                    stack.addLast(resource.getPathPattern());
                    processComponents(resource);

                    if (isRoot) {
                        Class likelyToBeRoot = null;
                        for (final Class next : resource.getHandlerClasses()) {
                            if (!(Inflector.class.isAssignableFrom(next))) {
                                likelyToBeRoot = next;
                            }
                        }

                        if (likelyToBeRoot != null) {
                            mappings.put(likelyToBeRoot, getMapping(getTemplate()));
                        }
                    }
                } finally {
                    stack.removeLast();
                }
            }

            @Override
            public void visitResourceMethod(final ResourceMethod resourceMethod) {

                if (resourceMethod.isExtended()) {
                    return;
                }

                if (ResourceMethod.JaxrsType.SUB_RESOURCE_LOCATOR.equals(resourceMethod.getType())) {
                    if (resourceMethod.getInvocable() != null) {
                        final Invocable i = resourceMethod.getInvocable();

                        final Type type = i.getResponseType();
                        final StringBuilder template = getTemplate();

                        mappings.put((Class) type, getMapping(template));

                        // Process sub resources ?

                        Resource.Builder builder = Resource
                                .builder(i.getRawResponseType());
                        if (builder == null) {
                            // for example in the case the return type of the sub resource locator is Object
                            builder = Resource.builder().path(resourceMethod.getParent().getPath());
                        }
                        final Resource subResource = builder.build();

                        visitChildResource(subResource);
                    }
                }

                processComponents(resourceMethod);
            }

            private StringBuilder getTemplate() {
                final StringBuilder template = new StringBuilder();
                for (final PathPattern pp : stack) {
                    final String ppTemplate = pp.getTemplate().getTemplate();

                    final int tlength = template.length();
                    if (tlength > 0) {
                        if (template.charAt(tlength - 1) == '/') {
                            if (ppTemplate.startsWith("/")) {
                                template.append(ppTemplate, 1, ppTemplate.length());
                            } else {
                                template.append(ppTemplate);
                            }
                        } else {
                            if (ppTemplate.startsWith("/")) {
                                template.append(ppTemplate);
                            } else {
                                template.append("/");
                                template.append(ppTemplate);
                            }
                        }
                    } else {
                        template.append(ppTemplate);
                    }

                }
                return template;
            }
        });

    }

    private Mapping getMapping(final StringBuilder template) {
        return new Mapping() {
            UriTemplate uriTemplate = new UriTemplate(template.toString());

            @Override
            public UriTemplate getTemplate() {
                return uriTemplate;
            }
        };
    }

}
