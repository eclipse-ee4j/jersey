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

package org.glassfish.jersey.linking;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.BeanParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.mapping.ResourceMappingContext;
import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.glassfish.jersey.server.model.MethodList;

/**
 * Utility class for working with {@link InjectLink} annotated fields.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
class InjectLinkFieldDescriptor extends FieldDescriptor implements InjectLinkDescriptor {

    private InjectLink link;
    private Class<?> type;
    private Map<String, String> bindings;

    /**
     * C'tor
     *
     * @param f the field to inject
     * @param l the InjectLink annotation
     * @param t the class that contains field f
     */
    InjectLinkFieldDescriptor(Field f, InjectLink l, Class<?> t) {
        super(f);
        link = l;
        type = t;
        bindings = new HashMap<>();
        for (Binding binding : l.bindings()) {
            bindings.put(binding.name(), binding.value());
        }
    }

    /**
     * Injects the uri into the field.
     *
     * @param instance the target for the injection
     * @param uri the value to inject
     */
    void setPropertyValue(Object instance, URI uri) {
        setAccessibleField(field);
        try {

            Object value;
            if (Objects.equals(URI.class, type)) {
                value = uri;
            } else if (Link.class.isAssignableFrom(type)) {

                // Make a link with the correct bindings
                value = getLink(uri);
            } else if (Objects.equals(String.class, type)) {
                value = uri.toString();
            } else {
                throw new IllegalArgumentException("Field type " + type + " not one of supported String,URI and Link");
            }

            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(InjectLinkFieldDescriptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Simple delegate to {@link InjectLink#style()}
     * @return {@link InjectLink#style()}
     */
    @Override
    public InjectLink.Style getLinkStyle() {
        return link.style();
    }

    /**
     * Returns the template based on the {@link ResourceMappingContext}
     *
     * @param rmc the context
     * @return the link template
     */
    @Override
    public String getLinkTemplate(ResourceMappingContext rmc) {
        return getLinkTemplate(rmc, link);
    }


    /**
     * Returns the template based on the {@link ResourceMappingContext}
     *
     * @param rmc the context
     * @param link the link
     * @return the link template
     */
    static String getLinkTemplate(ResourceMappingContext rmc, InjectLink link) {
        String template = null;
        if (Objects.equals(link.resource(), Class.class)) {
            template = link.value();
        } else {
            ResourceMappingContext.Mapping map = rmc.getMapping(link.resource());
            if (map != null) {
                template = map.getTemplate().getTemplate();
            } else {
                // extract template from specified class' @Path annotation
                Path path = link.resource().getAnnotation(Path.class);
                template = path == null ? "" : path.value();
            }

            // extract template from specified class' @Path annotation
            if (!link.method().isEmpty()) {
                // append value of method's @Path annotation
                MethodList methods = new MethodList(link.resource());
                methods = methods.withMetaAnnotation(HttpMethod.class);
                for (AnnotatedMethod method : methods) {
                    if (!Objects.equals(method.getMethod().getName(), link.method())) {
                        continue;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append(template);

                    Path methodPath = method.getAnnotation(Path.class);
                    if (methodPath != null) {
                        String methodTemplate = methodPath.value();

                        if (!(template.endsWith("/") || methodTemplate.startsWith("/"))) {
                            builder.append("/");
                        }
                        builder.append(methodTemplate);
                    }

                    CharSequence querySubString = extractQueryParams(method);

                    if (querySubString.length() > 0) {
                        builder.append("{?");
                        builder.append(querySubString);
                        builder.append("}");
                    }

                    template = builder.toString();
                    break;
                }
            }
        }

        return template;
    }

    static StringBuilder extractQueryParams(AnnotatedMethod method) throws SecurityException {
        // append query parameters
        StringBuilder querySubString = new StringBuilder();
        int parameterIndex = 0;
        for (Annotation[] paramAnns : method.getParameterAnnotations()) {
            for (Annotation ann : paramAnns) {
                if (Objects.equals(ann.annotationType(), QueryParam.class)) {
                    querySubString.append(((QueryParam) ann).value());
                    querySubString.append(',');
                }
                if (Objects.equals(ann.annotationType(), BeanParam.class)) {
                    Class<?> beanParamType = method.getParameterTypes()[parameterIndex];
                    Field[] fields = beanParamType.getFields();
                    for (Field field : fields) {
                        QueryParam queryParam = field.getAnnotation(QueryParam.class);
                        if (queryParam != null) {
                            querySubString.append(queryParam.value());
                            querySubString.append(',');
                        }
                    }
                    Method[] beanMethods = beanParamType.getMethods();
                    for (Method beanMethod : beanMethods) {
                        QueryParam queryParam = beanMethod.getAnnotation(QueryParam.class);
                        if (queryParam != null) {
                            querySubString.append(queryParam.value());
                            querySubString.append(',');
                        }
                    }
                }
            }
            parameterIndex++;
        }

        return querySubString;
    }

    /**
     * Creates a link from uri combined with {@link InjectLink}.
     *
     * @param uri uri for the link
     * @return Link instance
     */
    Link getLink(URI uri) {
        return InjectLink.Util.buildLinkFromUri(uri, link);
    }

    /**
     * Gets the binding by name
     *
     * @param name name of the binding
     * @return the binding
     */
    public String getBinding(String name) {
        return bindings.get(name);
    }


    /**
     * Returns the condition of {@link InjectLink}.
     * @return {@link InjectLink#condition()}
     */
    public String getCondition() {
        return link.condition();
    }
}
