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

package org.glassfish.jersey.inject.hk2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.Errors;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.inject.InjectionResolver;
import org.glassfish.jersey.internal.util.collection.ImmutableCollectors;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import org.glassfish.hk2.api.ClassAnalyzer;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Implementation of the {@link ClassAnalyzer} that supports selection
 * of the constructor with largest number of parameters as defined in
 * and required by JAX-RS specification.
 *
 * @author John Wells (john.wells at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
@Named(JerseyClassAnalyzer.NAME)
public final class JerseyClassAnalyzer implements ClassAnalyzer {

    /**
     * Name of the analyzer service.
     */
    public static final String NAME = "JerseyClassAnalyzer";

    /**
     * Binder for the JAX-RS compliant class analyzer implementation.
     */
    public static final class Binder extends AbstractBinder {

        private final ServiceLocator serviceLocator;

        /**
         * Constructor for {@code JerseyClassAnalyzer}.
         *
         * @param serviceLocator current injection manager.
         */
        public Binder(ServiceLocator serviceLocator) {
            this.serviceLocator = serviceLocator;
        }

        @Override
        protected void configure() {
            ClassAnalyzer defaultAnalyzer =
                    serviceLocator.getService(ClassAnalyzer.class, ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME);

            Supplier<List<InjectionResolver>> resolvers = () -> serviceLocator.getAllServices(InjectionResolver.class);

            bind(new JerseyClassAnalyzer(defaultAnalyzer, resolvers))
                    .analyzeWith(ClassAnalyzer.DEFAULT_IMPLEMENTATION_NAME)
                    .named(JerseyClassAnalyzer.NAME)
                    .to(ClassAnalyzer.class);
        }
    }

    private final ClassAnalyzer defaultAnalyzer;
    private final LazyValue<Set<Class>> resolverAnnotations;
    /**
     * Injection constructor.
     *
     * @param defaultAnalyzer   default HK2 class analyzer.
     * @param supplierResolvers configured injection resolvers.
     */
    private JerseyClassAnalyzer(ClassAnalyzer defaultAnalyzer, Supplier<List<InjectionResolver>> supplierResolvers) {
        this.defaultAnalyzer = defaultAnalyzer;
        Value<Set<Class>> resolvers = () -> supplierResolvers.get().stream()
                .filter(InjectionResolver::isConstructorParameterIndicator)
                .map(InjectionResolver::getAnnotation)
                .collect(ImmutableCollectors.toImmutableSet());
        this.resolverAnnotations = Values.lazy(resolvers);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Constructor<T> getConstructor(final Class<T> clazz) throws MultiException, NoSuchMethodException {
        if (clazz.isLocalClass()) {
            throw new NoSuchMethodException(LocalizationMessages.INJECTION_ERROR_LOCAL_CLASS_NOT_SUPPORTED(clazz.getName()));
        }
        if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            throw new NoSuchMethodException(
                    LocalizationMessages.INJECTION_ERROR_NONSTATIC_MEMBER_CLASS_NOT_SUPPORTED(clazz.getName()));
        }

        final Constructor<T> retVal;
        try {
            retVal = defaultAnalyzer.getConstructor(clazz);

            final Class<?>[] args = retVal.getParameterTypes();
            if (args.length != 0) {
                return retVal;
            }

            // Is zero length, but is it specifically marked?
            final Inject i = retVal.getAnnotation(Inject.class);
            if (i != null) {
                return retVal;
            }

            // In this case, the default chose a zero-arg constructor since it could find no other
        } catch (final NoSuchMethodException ignored) {
            // In this case, the default failed because it found no constructor it could use
        } catch (final MultiException me) {
            if (me.getErrors().size() != 1 && !(me.getErrors().get(0) instanceof IllegalArgumentException)) {
                throw me;
            }
            // Otherwise, the default failed because it found more than one constructor
        }

        // At this point, we simply need to find the constructor with the largest number of parameters
        final Constructor<?>[] constructors = AccessController.doPrivileged(
                (PrivilegedAction<Constructor<?>[]>) clazz::getDeclaredConstructors);
        Constructor<?> selected = null;
        int selectedSize = 0;
        int maxParams = -1;

        for (final Constructor<?> constructor : constructors) {
            final Class<?>[] params = constructor.getParameterTypes();
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
            throw new NoSuchMethodException(LocalizationMessages.INJECTION_ERROR_SUITABLE_CONSTRUCTOR_NOT_FOUND(clazz.getName()));
        }

        if (selectedSize > 1) {
            // Found {0} constructors with {1} parameters in {2} class. Selecting the first found constructor: {3}
            Errors.warning(clazz, LocalizationMessages.MULTIPLE_MATCHING_CONSTRUCTORS_FOUND(
                    selectedSize, maxParams, clazz.getName(), selected.toGenericString()));
        }

        return (Constructor<T>) selected;
    }

    @SuppressWarnings("MagicConstant")
    private boolean isCompatible(final Constructor<?> constructor) {
        if (constructor.getAnnotation(Inject.class) != null) {
            // JSR-330 applicable
            return true;
        }

        final int paramSize = constructor.getParameterTypes().length;

        if (paramSize != 0 && resolverAnnotations.get().isEmpty()) {
            return false;
        }

        if (!Modifier.isPublic(constructor.getModifiers())) {
            // return true for a default constructor, return false otherwise.
            return paramSize == 0
                    && (constructor.getDeclaringClass().getModifiers()
                                & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) == constructor.getModifiers();
        }

        for (final Annotation[] paramAnnotations : constructor.getParameterAnnotations()) {
            boolean found = false;
            for (final Annotation paramAnnotation : paramAnnotations) {
                if (resolverAnnotations.get().contains(paramAnnotation.annotationType())) {
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

    @Override
    public <T> Set<Method> getInitializerMethods(final Class<T> clazz) throws MultiException {
        return defaultAnalyzer.getInitializerMethods(clazz);
    }

    @Override
    public <T> Set<Field> getFields(final Class<T> clazz) throws MultiException {
        return defaultAnalyzer.getFields(clazz);
    }

    @Override
    public <T> Method getPostConstructMethod(final Class<T> clazz) throws MultiException {
        return defaultAnalyzer.getPostConstructMethod(clazz);
    }

    @Override
    public <T> Method getPreDestroyMethod(final Class<T> clazz) throws MultiException {
        return defaultAnalyzer.getPreDestroyMethod(clazz);
    }

}
