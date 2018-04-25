/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.mapping.ResourceMappingContext;
import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.glassfish.jersey.server.model.ResourceMethod;

/**
 * Utility to work with {@link ProvideLink} annotations.
 *
 * @author Leonard Brünings
 */
public class ProvideLinkDescriptor implements InjectLinkDescriptor {
    private final ProvideLink provideLink;
    private final ResourceMethod resource;

    private final Annotation parentAnnotation;

    private final Map<String, String> bindings;

    /**
     * c'tor
     *
     * @param resource the annotated resource method
     * @param provideLink the annotaion
     * @param parentAnnotation the parent annotation if present or {@code null}
     */
    public ProvideLinkDescriptor(ResourceMethod resource, ProvideLink provideLink, Annotation parentAnnotation) {
        this.provideLink = provideLink;
        this.resource = resource;
        this.parentAnnotation = parentAnnotation;
        bindings = new HashMap<>();
        for (Binding binding : provideLink.bindings()) {
            bindings.put(binding.name(), binding.value());
        }
    }

    /**
     * @return the annotation
     */
    public ProvideLink getProvideLink() {
        return provideLink;
    }

    /**
     * @return the annotated resource method
     */
    public ResourceMethod getResource() {
        return resource;
    }

    /**
     * Get the style
     *
     * @return the style
     */
    public InjectLink.Style getLinkStyle() {
        return provideLink.style();
    }

    /**
     * Get the link template, either directly from the value() or from the
     * {@code @Path} of the class referenced in resource()
     *
     * @return the link template
     */
    @Override
    public String getLinkTemplate(ResourceMappingContext rmc) {
        String template = null;
        ResourceMappingContext.Mapping map = rmc.getMapping(resource.getInvocable().getHandler().getHandlerClass());
        if (map != null) {
            template = map.getTemplate().getTemplate();
        } else {
            // extract template from specified class' @Path annotation
            Path path = resource.getInvocable().getHandler().getHandlerClass().getAnnotation(Path.class);
            template = path == null ? "" : path.value();
        }
        StringBuilder builder = new StringBuilder(template);

        Path methodPath = resource.getInvocable().getDefinitionMethod().getAnnotation(Path.class);
        if (methodPath != null) {
            String methodTemplate = methodPath.value();

            if (!(template.endsWith("/") || methodTemplate.startsWith("/"))) {
                builder.append("/");
            }
            builder.append(methodTemplate);
        }

        CharSequence querySubString = InjectLinkFieldDescriptor.extractQueryParams(
                new AnnotatedMethod(resource.getInvocable().getDefinitionMethod()));

        if (querySubString.length() > 0) {
            builder.append("{?");
            builder.append(querySubString);
            builder.append("}");
        }

        template = builder.toString();

        return template;
    }

    /**
     * Get the binding as an EL expression for a particular URI template parameter
     *
     * @param name binding name.
     * @return the EL binding.
     */
    @Override
    public String getBinding(String name) {
        return bindings.get(name);
    }

    /**
     * Get the condition.
     *
     * @return the condition
     */
    @Override
    public String getCondition() {
        return provideLink.condition();
    }

    /**
     * Builds a link from a {@link URI}.
     *
     * @param uri base URI
     * @return the {@link Link} instance
     */
    public Link getLink(URI uri) {
        return ProvideLink.Util.buildLinkFromUri(uri, provideLink);
    }

    /**
     * @return the parent annotation or {@code null}
     */
    public Annotation getParentAnnotation() {
        return parentAnnotation;
    }
}
