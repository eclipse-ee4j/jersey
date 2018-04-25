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

package org.glassfish.jersey.server.internal.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.Encoded;

import javax.inject.Provider;

import org.glassfish.jersey.internal.inject.Injectee;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * Abstract base class for resolving JAX-RS {@code &#64;XxxParam} injection.
 *
 * @param <A> supported parameter injection annotation.
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ParamInjectionResolver<A extends Annotation> implements InjectionResolver<A> {

    private final ValueParamProvider valueParamProvider;
    private final Class<A> annotation;
    private final Provider<ContainerRequest> request;

    /**
     * Initialize the base parameter injection resolver.
     *
     * @param valueParamProvider parameter value supplier provider.
     */
    public ParamInjectionResolver(ValueParamProvider valueParamProvider, Class<A> annotation,
            Provider<ContainerRequest> request) {
        this.valueParamProvider = valueParamProvider;
        this.annotation = annotation;
        this.request = request;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object resolve(Injectee injectee) {
        AnnotatedElement annotated = injectee.getParent();
        Annotation[] annotations;
        if (annotated.getClass().equals(Constructor.class)) {
            annotations = ((Constructor) annotated).getParameterAnnotations()[injectee.getPosition()];
        } else {
            annotations = annotated.getDeclaredAnnotations();
        }

        Class componentClass = injectee.getInjecteeClass();
        Type genericType = injectee.getRequiredType();

        final Type targetGenericType;
        if (injectee.isFactory()) {
            targetGenericType = ReflectionHelper.getTypeArgument(genericType, 0);
        } else {
            targetGenericType = genericType;
        }
        final Class<?> targetType = ReflectionHelper.erasure(targetGenericType);

        final Parameter parameter = Parameter.create(
                componentClass,
                componentClass,
                hasEncodedAnnotation(injectee),
                targetType,
                targetGenericType,
                annotations);

        final Function<ContainerRequest, ?> valueProvider = valueParamProvider.getValueProvider(parameter);
        if (valueProvider != null) {
            if (injectee.isFactory()) {
                return (Supplier<Object>) () -> valueProvider.apply(request.get());
            } else {
                return valueProvider.apply(request.get());
            }
        }

        return null;
    }

    private boolean hasEncodedAnnotation(Injectee injectee) {
        AnnotatedElement element = injectee.getParent();

        final boolean isConstructor = element instanceof Constructor;
        final boolean isMethod = element instanceof Method;

        // if injectee is method or constructor, check its parameters
        if (isConstructor || isMethod) {
            Annotation[] annotations;
            if (isMethod) {
                annotations = ((Method) element).getParameterAnnotations()[injectee.getPosition()];
            } else {
                annotations = ((Constructor) element).getParameterAnnotations()[injectee.getPosition()];
            }

            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Encoded.class)) {
                    return true;
                }
            }
        }

        // check injectee itself (method, constructor or field)
        if (element.isAnnotationPresent(Encoded.class)) {
            return true;
        }

        // check class which contains injectee
        Class<?> clazz = injectee.getInjecteeClass();
        return clazz.isAnnotationPresent(Encoded.class);
    }


    @Override
    public boolean isConstructorParameterIndicator() {
        return true;
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return false;
    }

    @Override
    public Class<A> getAnnotation() {
        return annotation;
    }
}
