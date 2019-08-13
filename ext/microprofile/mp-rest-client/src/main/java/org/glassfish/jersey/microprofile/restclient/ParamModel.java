/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.glassfish.jersey.model.Parameter;

/**
 * Abstract model for all elements with parameter annotation.
 *
 * @author David Kral
 * @author Tomas Langer
 */
abstract class ParamModel<T> {

    public static final Map<Class<? extends Annotation>,
            BiFunction<Builder, Annotation, ParamModel<?>>> PARAM_ANNOTATIONS = new HashMap<>();

    static {
        PARAM_ANNOTATIONS.put(PathParam.class,
                              (builder, annotation) -> new PathParamModel(builder, (PathParam) annotation));
        PARAM_ANNOTATIONS.put(HeaderParam.class,
                              (builder, annotation) -> new HeaderParamModel(builder, (HeaderParam) annotation));
        PARAM_ANNOTATIONS.put(CookieParam.class,
                              (builder, annotation) -> new CookieParamModel(builder, (CookieParam) annotation));
        PARAM_ANNOTATIONS.put(QueryParam.class,
                              (builder, annotation) -> new QueryParamModel(builder, (QueryParam) annotation));
        PARAM_ANNOTATIONS.put(MatrixParam.class,
                              (builder, annotation) -> new MatrixParamModel(builder, (MatrixParam) annotation));
        PARAM_ANNOTATIONS.put(FormParam.class,
                              (builder, annotation) -> new FormParamModel(builder, (FormParam) annotation));
        PARAM_ANNOTATIONS.put(BeanParam.class,
                              (builder, annotation) -> new BeanParamModel(builder));
    }

    protected final InterfaceModel interfaceModel;
    protected final Parameter parameter;
    private final Type type;
    private final AnnotatedElement annotatedElement;
    private final int paramPosition;
    private final boolean entity;

    /**
     * Processes parameter annotations and creates new instance of the model corresponding model.
     *
     * @param interfaceModel   model of the interface
     * @param type             annotated element type
     * @param annotatedElement annotated element
     * @param position         position in method params
     * @return new parameter instance
     */
    static ParamModel from(InterfaceModel interfaceModel, Type type, AnnotatedElement annotatedElement,
                           Parameter parameter, int position) {
        return new Builder(interfaceModel, type, annotatedElement, parameter, position).build();
    }

    ParamModel(Builder builder) {
        this.interfaceModel = builder.interfaceModel;
        this.type = builder.type;
        this.annotatedElement = builder.annotatedElement;
        this.entity = builder.entity;
        this.paramPosition = builder.paramPosition;
        this.parameter = builder.parameter;
    }

    /**
     * Returns {@link Type} of the parameter.
     *
     * @return parameter type
     */
    Type getType() {
        return type;
    }

    /**
     * Returns annotated element.
     *
     * @return annotated element
     */
    AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    int getParamPosition() {
        return paramPosition;
    }

    /**
     * Returns value if parameter is entity or not.
     *
     * @return if parameter is entity
     */
    boolean isEntity() {
        return entity;
    }

    /**
     * Transforms parameter to be part of the request.
     *
     * @param requestPart     part of a request
     * @param annotationClass annotation type
     * @param instance        actual method parameter value
     * @return updated request part
     */
    abstract T handleParameter(T requestPart, Class<? extends Annotation> annotationClass, Object instance);

    /**
     * Evaluates if the annotation passed in parameter is supported by this parameter.
     *
     * @param annotation checked annotation
     * @return if annotation is supported
     */
    abstract boolean handles(Class<? extends Annotation> annotation);

    protected static class Builder {

        private InterfaceModel interfaceModel;
        private Type type;
        private AnnotatedElement annotatedElement;
        private Parameter parameter;
        private boolean entity;
        private int paramPosition;

        private Builder(InterfaceModel interfaceModel,
                        Type type,
                        AnnotatedElement annotatedElement,
                        Parameter parameter,
                        int position) {
            this.interfaceModel = interfaceModel;
            this.type = type;
            this.annotatedElement = annotatedElement;
            this.parameter = parameter;
            this.paramPosition = position;
        }

        /**
         * Creates new ParamModel instance.
         *
         * @return new instance
         */
        ParamModel build() {
            for (Class<? extends Annotation> paramAnnotation : PARAM_ANNOTATIONS.keySet()) {
                Annotation annot = annotatedElement.getAnnotation(paramAnnotation);
                if (annot != null) {
                    return PARAM_ANNOTATIONS.get(paramAnnotation).apply(this, annot);
                }
            }

            entity = true;
            return new ParamModel<Object>(this) {
                @Override
                public Object handleParameter(Object requestPart, Class<? extends Annotation> annotationClass, Object instance) {
                    return requestPart;
                }

                @Override
                public boolean handles(Class<? extends Annotation> annotation) {
                    return false;
                }
            };
        }

    }

}
