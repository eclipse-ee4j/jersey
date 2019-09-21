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

package org.glassfish.jersey.inject.cdi.se.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.logging.Logger;

import javax.enterprise.inject.InjectionException;
import javax.inject.Inject;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

/**
 * Processes a provided class and selects the valid constructor with the largest number of parameters. Constructor is cached
 * for a later retrieve.
 */
public class CachedConstructorAnalyzer<T> {

    private static final Logger LOGGER = Logger.getLogger(CachedConstructorAnalyzer.class.getName());

    private final LazyValue<Constructor<T>> constructor;
    private final Collection<Class<? extends Annotation>> resolverAnnotations;

    /**
     * Creates a new constructor analyzer which accepts the class that is analyzed.
     *
     * @param clazz       analyzed class.
     * @param annotations all annotations used for an injecting.
     */
    public CachedConstructorAnalyzer(Class<T> clazz, Collection<Class<? extends Annotation>> annotations) {
        this.resolverAnnotations = annotations;
        this.constructor = Values.lazy((Value<Constructor<T>>) () -> getConstructorInternal(clazz));
    }

    public Constructor<T> getConstructor() {
        return constructor.get();
    }

    public boolean hasCompatibleConstructor() {
        try {
            return constructor.get() != null;
        } catch (InjectionException ex) {
            // Compatible constructor was not found.
            return false;
        }
    }

    /**
     * Select the proper constructor of the given {@code clazz}.
     *
     * @return compatible and largest constructor.
     */
    @SuppressWarnings("unchecked")
    private Constructor<T> getConstructorInternal(Class<T> clazz) {
        if (clazz.isLocalClass()) {
            throw new InjectionException(
                    LocalizationMessages.INJECTION_ERROR_LOCAL_CLASS_NOT_SUPPORTED(clazz.getName()));
        }
        if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            throw new InjectionException(
                    LocalizationMessages.INJECTION_ERROR_NONSTATIC_MEMBER_CLASS_NOT_SUPPORTED(clazz.getName()));
        }

        // At this point, we simply need to find the constructor with the largest number of parameters
        Constructor<?>[] constructors = AccessController.doPrivileged(
                (PrivilegedAction<Constructor<?>[]>) clazz::getDeclaredConstructors);
        Constructor<?> selected = null;
        int selectedSize = 0;
        int maxParams = -1;

        for (Constructor<?> constructor : constructors) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length >= maxParams && isCompatible(constructor)) {
                if (params.length > maxParams) {
                    maxParams = params.length;
                    selectedSize = 0;
                }

                selected = constructor;
                selectedSize++;
            }
        }

        if (selectedSize == 0) {
            throw new InjectionException(LocalizationMessages.INJECTION_ERROR_SUITABLE_CONSTRUCTOR_NOT_FOUND(clazz.getName()));
        }

        if (selectedSize > 1) {
            // Found {0} constructors with {1} parameters in {2} class. Selecting the first found constructor: {3}
            LOGGER.warning(LocalizationMessages.MULTIPLE_MATCHING_CONSTRUCTORS_FOUND(
                    selectedSize, maxParams, clazz.getName(), selected.toGenericString()));
        }

        return (Constructor<T>) selected;
    }

    /**
     * Checks whether the constructor is valid for injection that means that all parameters has an injection annotation.
     *
     * @param constructor constructor to inject.
     * @return True if element contains at least one inject annotation.
     */
    @SuppressWarnings("MagicConstant")
    private boolean isCompatible(Constructor<?> constructor) {
        if (constructor.getAnnotation(Inject.class) != null) {
            // JSR-330 applicable
            return true;
        }

        int paramSize = constructor.getParameterTypes().length;
        if (paramSize != 0 && resolverAnnotations.isEmpty()) {
            return false;
        }

        if (!Modifier.isPublic(constructor.getModifiers())) {
            // return true for a default constructor, return false otherwise.
            return paramSize == 0
                    && (constructor.getDeclaringClass().getModifiers()
                                & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) == constructor.getModifiers();
        }

        for (Annotation[] paramAnnotations : constructor.getParameterAnnotations()) {
            boolean found = false;
            for (Annotation paramAnnotation : paramAnnotations) {
                if (resolverAnnotations.contains(paramAnnotation.annotationType())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }
}
