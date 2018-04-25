/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.validation.internal;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.validation.Path;
import javax.validation.TraversableResolver;

import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * {@link TraversableResolver Traversable resolver} used for handling {@link javax.validation.executable.ValidateOnExecution}
 * annotations present on property getters when validating resource class.
 *
 * @author Michal Gajdos
 */
class ValidateOnExecutionTraversableResolver implements TraversableResolver {

    private final TraversableResolver delegate;

    private final ConcurrentMap<String, Method> propertyToMethod = new ConcurrentHashMap<>();

    private final ValidateOnExecutionHandler validateOnExecutionHandler;

    private final boolean validateExecutable;

    /**
     * Create a new {@link ValidateOnExecutionTraversableResolver}.
     *
     * @param delegate delegate requests to this underlying traversable resolver if this one cannot resolve it.
     * @param validateOnExecutionHandler handler to determine whether a getter should be validated or not.
     * @param validateExecutable bootstrap flag to enable/disable global validation of executables.
     */
    public ValidateOnExecutionTraversableResolver(final TraversableResolver delegate,
                                                  final ValidateOnExecutionHandler validateOnExecutionHandler,
                                                  final boolean validateExecutable) {
        this.delegate = delegate;
        this.validateExecutable = validateExecutable;
        this.validateOnExecutionHandler = validateOnExecutionHandler;
    }

    @Override
    public boolean isReachable(final Object traversableObject,
                               final Path.Node traversableProperty,
                               final Class<?> rootBeanType,
                               final Path pathToTraversableObject,
                               final ElementType elementType) {
        // Make sure only getters on entities are validated (not getters on resource classes).
        final Class<?> traversableObjectClass = traversableObject.getClass();
        final boolean isEntity = !rootBeanType.equals(traversableObjectClass);

        if (isEntity && validateExecutable && ElementType.METHOD.equals(elementType)) {
            final String propertyName = traversableProperty.getName();
            final String propertyKey = traversableObjectClass.getName() + "#" + propertyName;

            if (!propertyToMethod.containsKey(propertyKey)) {
                final Method getter = getGetterMethod(traversableObjectClass, propertyName);

                if (getter != null) {
                    propertyToMethod.putIfAbsent(propertyKey, getter);
                }
            }

            final Method getter = propertyToMethod.get(propertyKey);
            return getter != null && validateOnExecutionHandler.validateGetter(traversableObjectClass, getter);
        }

        return delegate.isReachable(traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType);
    }

    @Override
    public boolean isCascadable(final Object traversableObject,
                                final Path.Node traversableProperty,
                                final Class<?> rootBeanType,
                                final Path pathToTraversableObject,
                                final ElementType elementType) {
        return delegate.isCascadable(traversableObject, traversableProperty, rootBeanType, pathToTraversableObject, elementType);
    }

    /**
     * Return getter method defined on {@code clazz} of property with given {@code propertyName}.
     *
     * @param clazz class to find a getter method on.
     * @param propertyName name of the property to find a getter for.
     * @return getter method or {@code null} if the method cannot be found.
     */
    private Method getGetterMethod(final Class<?> clazz, final String propertyName) {
        // Property type.
        Class<?> propertyType = null;
        for (final Field field : AccessController.doPrivileged(ReflectionHelper.getAllFieldsPA(clazz))) {
            if (field.getName().equals(propertyName)) {
                propertyType = field.getType();
            }
        }

        final char[] chars = propertyName.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        final String getterPropertyName = new String(chars);

        final String isGetter = "is" + getterPropertyName;
        final String getGetter = "get" + getterPropertyName;

        for (final Method method : AccessController.doPrivileged(ReflectionHelper.getMethodsPA(clazz))) {
            final String methodName = method.getName();

            if ((methodName.equals(isGetter) || methodName.equals(getGetter))
                    && ReflectionHelper.isGetter(method)
                    && (propertyType == null || propertyType.isAssignableFrom(method.getReturnType()))) {
                return AccessController.doPrivileged(ReflectionHelper.findMethodOnClassPA(clazz, method));
            }
        }

        return null;
    }
}
