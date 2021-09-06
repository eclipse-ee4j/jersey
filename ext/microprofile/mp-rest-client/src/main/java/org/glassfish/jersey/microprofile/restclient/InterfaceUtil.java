/*
 * Copyright (c) 2019, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.HttpMethod;

import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * Utils for interface handling.
 *
 * @author David Kral
 */
class InterfaceUtil {

    private static final String PARAMETER_PARSE_REGEXP = "(?<=\\{).+?(?=\\})";
    private static final Pattern PATTERN = Pattern.compile(PARAMETER_PARSE_REGEXP);

    /**
     * Parses all required parameters from template string.
     *
     * @param template template string
     * @return parsed parameters
     */
    static List<String> parseParameters(String template) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = PATTERN.matcher(template);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }

    /**
     * Parses all required parameters from expr string.
     * Parameters encapsulated by {} or parameters with regexp expressions like {param: (regex_here)}
     *
     * @param expr string expression
     * @return path params
     */
    static List<String> getAllMatchingParams(String expr) {
        List<String> allMatches = new ArrayList<>();
        if (expr == null || expr.isEmpty() || expr.indexOf('{') == -1) {
            return allMatches;
        }

        boolean matching = false;
        int parenthesisMatched = 0;
        StringBuilder matchingParameter = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char x = expr.charAt(i);

            if (!matching && x == '{' && parenthesisMatched == 0) {
                matching = true;
            } else if (matching && x != ':' && x != '}') {
                matchingParameter.append(x);
            } else if (matching) {
                allMatches.add(matchingParameter.toString());
                matchingParameter.setLength(0);
                matching = false;
            }

            if (x == '}') {
                parenthesisMatched--;
            } else if (x == '{') {
                parenthesisMatched++;
            }
        }
        return allMatches;
    }

    /**
     * Validates and returns proper compute method defined in {@link ClientHeaderParam}.
     *
     * @param iClass interface class
     * @param headerValue value of the header
     * @return parsed method
     */
    static Method parseComputeMethod(Class<?> iClass, String[] headerValue) {
        List<String> computeMethodNames = InterfaceUtil.parseParameters(Arrays.toString(headerValue));
        /*if more than one string is specified as the value attribute, and one of the strings is a
          compute method (surrounded by curly braces), then the implementation will throw a
          RestClientDefinitionException*/
        if (headerValue.length > 1 && computeMethodNames.size() > 0) {
            throw new RestClientDefinitionException("@ClientHeaderParam annotation should not contain compute method "
                                                            + "when multiple values are present in value attribute. "
                                                            + "See " + iClass.getName());
        }
        if (computeMethodNames.size() == 1) {
            String methodName = computeMethodNames.get(0);
            List<Method> computeMethods = getAnnotationComputeMethod(iClass, methodName);
            if (computeMethods.size() != 1) {
                throw new RestClientDefinitionException("No valid compute method found for name: " + methodName);
            }
            return computeMethods.get(0);
        }
        return null;
    }

    private static List<Method> getAnnotationComputeMethod(Class<?> iClass, String methodName) {
        if (methodName.contains(".")) {
            return getStaticComputeMethod(methodName);
        }
        return getComputeMethod(iClass, methodName);
    }

    private static List<Method> getStaticComputeMethod(String methodName) {
        int lastIndex = methodName.lastIndexOf(".");
        String className = methodName.substring(0, lastIndex);
        String staticMethodName = methodName.substring(lastIndex + 1);
        Class<?> classWithStaticMethod = AccessController.doPrivileged(ReflectionHelper.classForNamePA(className));
        if (classWithStaticMethod == null) {
            throw new IllegalStateException("No class with following name found: " + className);
        }
        return getComputeMethod(classWithStaticMethod, staticMethodName);
    }

    private static List<Method> getComputeMethod(Class<?> iClass, String methodName) {
        return Arrays.stream(iClass.getMethods())
                // filter out methods with specified name only
                .filter(method -> method.getName().equals(methodName))
                // filter out other methods than default and static
                .filter(method -> method.isDefault() || Modifier.isStatic(method.getModifiers()))
                // filter out methods without required return type
                .filter(method -> method.getReturnType().equals(String.class)
                        || method.getReturnType().equals(String[].class))
                // filter out methods without required parameter types
                .filter(method -> method.getParameterTypes().length == 0 || (
                        method.getParameterTypes().length == 1
                                && method.getParameterTypes()[0].equals(String.class)))
                .collect(Collectors.toList());
    }

    /**
     * Returns {@link List} of annotations which are type of {@link HttpMethod}.
     *
     * @param annotatedElement element with annotations
     * @return annotations of given type
     */
    static List<Class<?>> getHttpAnnotations(AnnotatedElement annotatedElement) {
        return Arrays.stream(annotatedElement.getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().getAnnotation(HttpMethod.class) != null)
                .map(Annotation::annotationType)
                .collect(Collectors.toList());
    }
}
