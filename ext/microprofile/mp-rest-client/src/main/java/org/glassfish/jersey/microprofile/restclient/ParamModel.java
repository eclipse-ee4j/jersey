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
 */
abstract class ParamModel<T> {

    protected final InterfaceModel interfaceModel;
    protected final Parameter parameter;
    private final Type type;
    private final AnnotatedElement annotatedElement;
    private final int paramPosition;
    private final boolean entity;

    /**
     * Processes parameter annotations and creates new instance of the model corresponding model.
     *
     * @param interfaceModel model of the interface
     * @param type annotated element type
     * @param annotatedElement annotated element
     * @param position position in method params
     * @return new parameter instance
     */
    static ParamModel from(InterfaceModel interfaceModel, Type type, AnnotatedElement annotatedElement,
                           Parameter parameter, int position) {
        return new Builder(interfaceModel, type, annotatedElement, parameter)
                .pathParamName(annotatedElement.getAnnotation(PathParam.class))
                .headerParamName(annotatedElement.getAnnotation(HeaderParam.class))
                .beanParam(annotatedElement.getAnnotation(BeanParam.class))
                .cookieParam(annotatedElement.getAnnotation(CookieParam.class))
                .queryParam(annotatedElement.getAnnotation(QueryParam.class))
                .matrixParam(annotatedElement.getAnnotation(MatrixParam.class))
                .formParam(annotatedElement.getAnnotation(FormParam.class))
                .paramPosition(position)
                .build();
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
     * @param requestPart part of a request
     * @param annotationClass annotation type
     * @param instance actual method parameter value
     * @return updated request part
     */
    abstract T handleParameter(T requestPart, Class<?> annotationClass, Object instance);

    /**
     * Evaluates if the annotation passed in parameter is supported by this parameter.
     *
     * @param annotation checked annotation
     * @return if annotation is supported
     */
    abstract boolean handles(Class<Annotation> annotation);

    protected static class Builder {

        private InterfaceModel interfaceModel;
        private Type type;
        private AnnotatedElement annotatedElement;
        private Parameter parameter;
        private String pathParamName;
        private String headerParamName;
        private String cookieParamName;
        private String queryParamName;
        private String matrixParamName;
        private String formParamName;
        private boolean beanParam;
        private boolean entity;
        private int paramPosition;

        private Builder(InterfaceModel interfaceModel, Type type, AnnotatedElement annotatedElement, Parameter parameter) {
            this.interfaceModel = interfaceModel;
            this.type = type;
            this.annotatedElement = annotatedElement;
            this.parameter = parameter;
        }

        /**
         * Path parameter name.
         *
         * @param pathParam {@link PathParam} annotation
         * @return updated Builder instance
         */
        Builder pathParamName(PathParam pathParam) {
            this.pathParamName = pathParam == null ? null : pathParam.value();
            return this;
        }

        /**
         * Header parameter name.
         *
         * @param headerParam {@link HeaderParam} annotation
         * @return updated Builder instance
         */
        Builder headerParamName(HeaderParam headerParam) {
            this.headerParamName = headerParam == null ? null : headerParam.value();
            return this;
        }

        /**
         * Bean parameter identifier.
         *
         * @param beanParam {@link BeanParam} annotation
         * @return updated Builder instance
         */
        Builder beanParam(BeanParam beanParam) {
            this.beanParam = beanParam != null;
            return this;
        }

        /**
         * Cookie parameter.
         *
         * @param cookieParam {@link CookieParam} annotation
         * @return updated Builder instance
         */
        Builder cookieParam(CookieParam cookieParam) {
            this.cookieParamName = cookieParam == null ? null : cookieParam.value();
            return this;
        }

        /**
         * Query parameter.
         *
         * @param queryParam {@link QueryParam} annotation
         * @return updated Builder instance
         */
        Builder queryParam(QueryParam queryParam) {
            this.queryParamName = queryParam == null ? null : queryParam.value();
            return this;
        }

        /**
         * Matrix parameter.
         *
         * @param matrixParam {@link MatrixParam} annotation
         * @return updated Builder instance
         */
        Builder matrixParam(MatrixParam matrixParam) {
            this.matrixParamName = matrixParam == null ? null : matrixParam.value();
            return this;
        }

        /**
         * Form parameter.
         *
         * @param formParam {@link FormParam} annotation
         * @return updated Builder instance
         */
        Builder formParam(FormParam formParam) {
            this.formParamName = formParam == null ? null : formParam.value();
            return this;
        }

        /**
         * Position of parameter in method parameters
         *
         * @param paramPosition Parameter position
         * @return updated Builder instance
         */
        Builder paramPosition(int paramPosition) {
            this.paramPosition = paramPosition;
            return this;
        }

        /**
         * Returns path param name;
         *
         * @return path param name
         */
        String pathParamName() {
            return pathParamName;
        }

        /**
         * Returns header param name;
         *
         * @return header param name
         */
        String headerParamName() {
            return headerParamName;
        }

        String cookieParamName() {
            return cookieParamName;
        }

        String queryParamName() {
            return queryParamName;
        }

        String matrixParamName() {
            return matrixParamName;
        }

        String formParamName() {
            return matrixParamName;
        }

        /**
         * Creates new ParamModel instance.
         *
         * @return new instance
         */
        ParamModel build() {
            if (pathParamName != null) {
                return new PathParamModel(this);
            } else if (headerParamName != null) {
                return new HeaderParamModel(this);
            } else if (beanParam) {
                return new BeanParamModel(this);
            } else if (cookieParamName != null) {
                return new CookieParamModel(this);
            } else if (queryParamName != null) {
                return new QueryParamModel(this);
            } else if (matrixParamName != null) {
                return new MatrixParamModel(this);
            } else if (formParamName != null) {
                return new FormParamModel(this);
            }
            entity = true;
            return new ParamModel(this) {
                @Override
                public Object handleParameter(Object requestPart, Class annotationClass, Object instance) {
                    return requestPart;
                }

                @Override
                public boolean handles(Class annotation) {
                    return false;
                }
            };
        }

    }

}
