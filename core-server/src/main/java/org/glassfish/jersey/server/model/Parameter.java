/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Encoded;

import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.model.internal.spi.ParameterServiceProvider;
import org.glassfish.jersey.server.Uri;

/**
 * Method parameter model.
 *
 * @author Marek Potociar
 */
public class Parameter extends org.glassfish.jersey.model.Parameter implements AnnotatedElement {

    /**
     * Create a parameter model.
     *
     * @param concreteClass   concrete resource method handler implementation class.
     * @param declaringClass  declaring class of the method the parameter belongs to or field that this parameter represents.
     * @param encodeByDefault flag indicating whether the parameter should be encoded by default or not. Note that a presence
     *                        of {@link Encoded} annotation in the list of the parameter {@code annotations} will override any
     *                        value set in the flag to {@code true}.
     * @param rawType         raw Java parameter type.
     * @param type            generic Java parameter type.
     * @param annotations     parameter annotations.
     * @return new parameter model.
     */
    @SuppressWarnings("unchecked")
    public static <PARAMETER extends org.glassfish.jersey.model.Parameter> PARAMETER create(
            Class concreteClass,
            Class declaringClass,
            boolean encodeByDefault,
            Class<?> rawType,
            Type type,
            Annotation[] annotations) {
        return (PARAMETER) create(concreteClass, declaringClass, encodeByDefault, rawType, type, annotations, Parameter.class);
    }

    protected static <PARAMETER extends org.glassfish.jersey.model.Parameter> List<PARAMETER> createList(
            Class concreteClass,
            Class declaringClass,
            boolean keepEncoded,
            Class[] parameterTypes,
            Type[] genericParameterTypes,
            Annotation[][] parameterAnnotations,
            Class<?> parameterClass) {

        final List<PARAMETER> parameters = new ArrayList<>(parameterTypes.length);

        for (int i = 0; i < parameterTypes.length; i++) {
            final PARAMETER parameter = Parameter.create(
                    concreteClass,
                    declaringClass,
                    keepEncoded,
                    parameterTypes[i],
                    genericParameterTypes[i],
                    parameterAnnotations[i]);
            if (null != parameter) {
                parameters.add(parameter);
            } else {
                // TODO throw IllegalStateException instead?
                return Collections.emptyList();
            }
        }

        return parameters;
    }

    /**
     * Create a list of parameter models for a given resource method handler
     * injectable constructor.
     *
     * @param concreteClass  concrete resource method handler implementation class.
     * @param declaringClass class where the method has been declared.
     * @param ctor           injectable constructor of the resource method handler.
     * @param keepEncoded    set to {@code true} to disable automatic decoding
     *                       of all the constructor parameters. (See {@link Encoded}.
     * @return a list of constructor parameter models.
     */
    public static <PARAMETER extends org.glassfish.jersey.model.Parameter> List<PARAMETER> create(
            Class concreteClass,
            Class declaringClass,
            Constructor<?> ctor,
            boolean keepEncoded) {
        return createList(concreteClass, declaringClass, ctor, keepEncoded, Parameter.class);
    }

    /**
     * Create a list of parameter models for a given Java method handling a resource
     * method, sub-resource method or a sub-resource locator.
     *
     * @param concreteClass  concrete resource method handler implementation class.
     * @param declaringClass the class declaring the handling Java method.
     * @param javaMethod     Java method handling a resource method, sub-resource
     *                       method or a sub-resource locator.
     * @param keepEncoded    set to {@code true} to disable automatic decoding
     *                       of all the method parameters. (See {@link Encoded}.
     * @return a list of handling method parameter models.
     */
    public static <PARAMETER extends org.glassfish.jersey.model.Parameter> List<PARAMETER> create(
            Class concreteClass,
            Class declaringClass,
            Method javaMethod,
            boolean keepEncoded) {
        return createList(concreteClass, declaringClass, javaMethod, keepEncoded, Parameter.class);
    }

    /**
     * Create new parameter model by overriding {@link Parameter.Source source}
     * of the original parameter model.
     *
     * @param original original parameter model.
     * @param source   new overriding parameter source.
     * @return source-overridden copy of the original parameter.
     */
    public static Parameter overrideSource(Parameter original, Parameter.Source source) {

        return new Parameter(
                original.getAnnotations(),
                original.getSourceAnnotation(),
                source,
                source.name(),
                original.getRawType(),
                original.getType(),
                original.isEncoded(),
                original.getDefaultValue());
    }


    protected Parameter(
            Annotation[] markers,
            Annotation marker,
            Source source,
            String sourceName,
            Class<?> rawType,
            Type type,
            boolean encoded,
            String defaultValue) {
        super(markers, marker, source, sourceName, rawType, type, encoded, defaultValue);
    }

    /**
     * Bean Parameter class represents a parameter annotated with {@link BeanParam} which in fact represents
     * additional set of parameters.
     */
    public static class BeanParameter extends Parameter {

        private final Collection<Parameter> parameters;

        private BeanParameter(final Annotation[] markers,
                              final Annotation marker,
                              final String sourceName,
                              final Class<?> rawType,
                              final Type type, final boolean encoded, final String defaultValue) {
            super(markers, marker, Source.BEAN_PARAM, sourceName, rawType, type, encoded, defaultValue);

            final Collection<Parameter> parameters = new LinkedList<>();

            for (Field field : AccessController.doPrivileged(ReflectionHelper.getDeclaredFieldsPA(rawType))) {
                if (field.getDeclaredAnnotations().length > 0) {
                    Parameter beanParamParameter = Parameter.create(
                            rawType,
                            field.getDeclaringClass(),
                            field.isAnnotationPresent(Encoded.class),
                            field.getType(),
                            field.getGenericType(),
                            field.getAnnotations());
                    parameters.add(beanParamParameter);
                }
            }
            for (Constructor constructor : AccessController
                    .doPrivileged(ReflectionHelper.getDeclaredConstructorsPA(rawType))) {
                for (org.glassfish.jersey.model.Parameter parameter : Parameter.create(rawType, rawType, constructor, false)) {
                    parameters.add((Parameter) parameter);
                }
            }

            this.parameters = Collections.unmodifiableCollection(parameters);
        }

        /**
         * @return The transitively associated parameters through this {@link BeanParam} parameter.
         */
        public Collection<Parameter> getParameters() {
            return parameters;
        }
    }

    /**
     * Check if the parameter is {@link ParamQualifier qualified}.
     *
     * @return {@code true} if the parameter is qualified, {@code false} otherwise.
     */
    public boolean isQualified() {
        for (Annotation a : getAnnotations()) {
            if (a.annotationType().isAnnotationPresent(ParamQualifier.class)) {
                return true;
            }
        }
        return false;
    }

    public static class ServerParameterService implements ParameterServiceProvider {
        @Override
        public Map<Class, ParamAnnotationHelper> getParameterAnnotationHelperMap() {
            Map<Class, ParamAnnotationHelper> m = new WeakHashMap<Class, ParamAnnotationHelper>();
            m.put(Uri.class, new ParamAnnotationHelper<Uri>() {

                @Override
                public String getValueOf(Uri a) {
                    return a.value();
                }

                @Override
                public Parameter.Source getSource() {
                    return Parameter.Source.URI;
                }
            });
            return m;
        }

        @Override
        public ParamCreationFactory<Parameter> getParameterCreationFactory() {
            return new ParamCreationFactory<Parameter>() {
                @Override
                public boolean isFor(Class<?> clazz) {
                    return clazz == Parameter.class;
                }

                @Override
                public Parameter createParameter(Annotation[] markers, Annotation marker, Source source, String sourceName,
                                                 Class<?> rawType, Type type, boolean encoded, String defaultValue) {
                    return new Parameter(markers, marker, source, sourceName, rawType, type, encoded, defaultValue);
                }

                @Override
                public Parameter createBeanParameter(Annotation[] markers, Annotation marker, Source source, String sourceName,
                                                     Class<?> rawType, Type type, boolean encoded, String defaultValue) {
                    return new BeanParameter(markers, marker, sourceName, rawType, type, encoded, defaultValue);
                }
            };
        }
    }
}
