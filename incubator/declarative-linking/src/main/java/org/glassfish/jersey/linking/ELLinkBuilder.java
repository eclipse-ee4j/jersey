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

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.glassfish.jersey.linking.mapping.ResourceMappingContext;
import org.glassfish.jersey.uri.internal.UriTemplateParser;

/**
 * A helper class to build links from EL expressions.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
final class ELLinkBuilder {

    private ELLinkBuilder() {
    }

    private static final ExpressionFactory expressionFactory =
            ExpressionFactory.newInstance();

    /**
     * Evaluates the condition
     *
     * @param condition the condition expression
     * @param entity    the entity returned from the resource method
     * @param resource  the resource class instance that returned the entity
     * @param instance  the instance that contains the entity, e.g. the value of a field within an entity class.
     * @return the result of the condition
     */
    static boolean evaluateCondition(String condition,
                                     Object entity,
                                     Object resource,
                                     Object instance) {

        if (condition == null || condition.isEmpty()) {
            return true;
        }
        LinkELContext context = new LinkELContext(entity, resource, instance);
        ValueExpression expr = expressionFactory.createValueExpression(context, condition, boolean.class);

        Object result = expr.getValue(context).toString();
        return "true".equals(result);
    }

    /**
     * Creates the URI using the link descriptor.
     *
     * @param link the link descriptor
     * @param entity the entity returned from the resource method
     * @param resource the resource class instance that returned the entity
     * @param instance the instance that contains the entity, e.g. the value of a field within an entity class.
     * @param uriInfo JAX-RS {@link UriInfo}
     * @param rmc the {@link ResourceMappingContext}
     * @return the URI
     */
    static URI buildURI(InjectLinkDescriptor link,
                        Object entity,
                        Object resource,
                        Object instance,
                        UriInfo uriInfo,
                        ResourceMappingContext rmc) {

        String template = link.getLinkTemplate(rmc);

        // first process any embedded EL expressions
        LinkELContext context = new LinkELContext(entity, resource, instance);
        ValueExpression expr = expressionFactory.createValueExpression(context,
                template, String.class);
        template = expr.getValue(context).toString();

        // now process any embedded URI template parameters
        UriBuilder ub = applyLinkStyle(template, link.getLinkStyle(), uriInfo);
        UriTemplateParser parser = new UriTemplateParser(template);
        List<String> parameterNames = parser.getNames();
        Map<String, Object> valueMap = getParameterValues(parameterNames, link, context, uriInfo);
        return ub.buildFromMap(valueMap);
    }

    private static UriBuilder applyLinkStyle(String template, InjectLink.Style style, UriInfo uriInfo) {
        UriBuilder ub = null;
        switch (style) {
            case ABSOLUTE:
                ub = uriInfo.getBaseUriBuilder().path(template);
                break;
            case ABSOLUTE_PATH:
                String basePath = uriInfo.getBaseUri().getPath();
                ub = UriBuilder.fromPath(basePath).path(template);
                break;
            case RELATIVE_PATH:
                ub = UriBuilder.fromPath(template);
                break;
        }
        return ub;
    }

    private static Map<String, Object> getParameterValues(List<String> parameterNames,
                                                          InjectLinkDescriptor linkField,
                                                          LinkELContext context,
                                                          UriInfo uriInfo) {
        Map<String, Object> values = new HashMap<>();
        for (String name : parameterNames) {
            String elExpression = linkField.getBinding(name);
            if (elExpression == null) {
                String value = uriInfo.getPathParameters().getFirst(name);
                if (value == null) {
                    value = uriInfo.getQueryParameters().getFirst(name);
                }
                if (value != null) {
                    values.put(name, value);
                    continue;
                }
                elExpression = "${" + ResponseContextResolver.INSTANCE_OBJECT + "." + name + "}";
            }
            ValueExpression expr = expressionFactory.createValueExpression(context,
                        elExpression, String.class);

            Object value = expr.getValue(context);
            values.put(name, value != null ? value.toString() : null);
         }
        return values;
    }

}
