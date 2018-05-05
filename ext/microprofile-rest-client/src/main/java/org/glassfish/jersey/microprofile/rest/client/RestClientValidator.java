/*
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.microprofile.rest.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;

public class RestClientValidator {

    private static volatile RestClientValidator instance;

    private RestClientValidator() {
    }

    public static RestClientValidator getInstance() {
        if (instance == null) {
            synchronized (RestClientValidator.class) {
                if (instance == null) {
                    instance = new RestClientValidator();
                }
            }
        }
        return instance;
    }

    /**
     * Invalid client interfaces will result in a RestClientDefinitionException
     *
     * @param restClient
     */
    public void validate(Class<?> restClient) {
        if (!restClient.isInterface()) {
            throw new IllegalArgumentException(
                    String.format("Rest Client [%s] must be interface", restClient)
            );
        }
        Method[] methods = restClient.getMethods();
        checkMethodsForMultipleHTTPMethodAnnotations(restClient, methods);
        checkMethodsForInvalidURITemplates(restClient, methods);
    }

    private void checkMethodsForMultipleHTTPMethodAnnotations(Class<?> restClient, Method[] methods)
            throws RestClientDefinitionException {
        // using multiple HTTP method annotations on the same method
        for (Method method : methods) {
            List<String> httpMethods = new ArrayList<>();
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType()
                        .getAnnotation(HttpMethod.class) != null) {
                    httpMethods.add(annotation.annotationType().getSimpleName());
                }
                if (httpMethods.size() > 1) {
                    throw new RestClientDefinitionException(
                            String.format(
                                    "Rest Client [%s] method [%s] contains multiple HTTP method annotations %s",
                                    restClient,
                                    method.getName(),
                                    httpMethods
                            )
                    );
                }

            }
        }
    }

    private void checkMethodsForInvalidURITemplates(Class<?> restClient, Method[] methods)
            throws RestClientDefinitionException {

        // invalid parameter
        Path classLevelPath = restClient.getAnnotation(Path.class);

        final Set<String> classLevelVariables = new HashSet<>();
        UriBuilder classBuilder = null;
        if (classLevelPath != null) {
            classBuilder = UriBuilder.fromUri(classLevelPath.value());
            classLevelVariables.addAll(getPathParams(classLevelPath));
        }

        // invalid URI templates
        for (Method method : methods) {
            UriBuilder builder;

            Set<String> pathParam = new HashSet<>(classLevelVariables);
            Path methodLevelPath = method.getAnnotation(Path.class);
            if (classLevelPath == null && methodLevelPath == null) {
                continue;
            } else if (methodLevelPath != null) {
                builder = UriBuilder.fromUri(classLevelPath == null
                        ? methodLevelPath.value() : classLevelPath.value() + "/" + methodLevelPath.value()
                );
                pathParam.addAll(getPathParams(methodLevelPath));
            } else {
                builder = classBuilder;
            }

            Map<String, Object> variableParam = getVariableParam(method);
            if (pathParam.size() != variableParam.size()) {
                throw new RestClientDefinitionException(
                        String.format(
                                "Rest Client [%s] method [%s] parameters %s are not matched with path variables %s",
                                restClient, method.getName(), variableParam.keySet(), pathParam
                        )
                );
            }
            try {
                builder.resolveTemplates(variableParam, false).build();
            } catch (IllegalArgumentException ex) {
                throw new RestClientDefinitionException(
                        String.format(
                                "Rest Client [%s] method [%s] parameters %s are not matched with path variables %s",
                                restClient, method.getName(), variableParam.keySet(), pathParam
                        ), ex
                );
            }

        }
    }

    private Map<String, Object> getVariableParam(Method method) {
        Map<String, Object> params = new HashMap<>();
        for (Parameter p : method.getParameters()) {
            PathParam pathParam = p.getAnnotation(PathParam.class);
            if (pathParam != null) {
                params.put(pathParam.value(), "");
            }
        }
        return params;
    }

    private List<String> getPathParams(Path path) {
        List<String> params = new ArrayList<>();
        Pattern p = Pattern.compile("\\{(.*?)\\}");
        Matcher m = p.matcher(path.value());

        while (m.find()) {
            params.add(m.group(1));
        }
        return params;
    }

}
