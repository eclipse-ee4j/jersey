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

package org.glassfish.jersey.tests.cdi.bv;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;

/**
 * Helper class to implement support for {@code javax.mvc.validation.ValidationResult}.
 *
 * @author Santiago Pericas-Geertsen
 */
@Vetoed
public final class ValidationResultUtil {

    private static final String VALIDATION_RESULT = ValidationResult.class.getName();

    private ValidationResultUtil() {
        throw new AssertionError("Instantiation not allowed.");
    }

    /**
     * Search for a {@code javax.mvc.validation.ValidationResult} field in the resource's
     * class hierarchy. Field must be annotated with {@link javax.inject.Inject}.
     *
     * @param resource resource instance.
     * @return field or {@code null} if none is found.
     */
    public static Field getValidationResultField(final Object resource) {
        Class<?> clazz = resource.getClass();
        do {
            for (Field f : clazz.getDeclaredFields()) {
                // Of ValidationResult and CDI injectable
                if (f.getType().getName().equals(VALIDATION_RESULT)
                        && f.getAnnotation(Inject.class) != null) {
                    return f;
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class);
        return null;
    }

    /**
     * Updates a {@code javax.mvc.validation.ValidationResult} field. In pseudo-code:
     * <p/>
     * resource.field.setViolations(constraints)
     *
     * @param resource    resource instance.
     * @param field       field to be updated.
     * @param constraints new set of constraints.
     */
    public static void updateValidationResultField(Object resource, Field field,
                                                   Set<ConstraintViolation<?>> constraints) {
        try {
            field.setAccessible(true);
            final Object obj = field.get(resource);
            Method setter;
            try {
                setter = obj.getClass().getMethod("setViolations", Set.class);
            } catch (NoSuchMethodException e) {
                setter = obj.getClass().getSuperclass().getMethod("setViolations", Set.class);
            }
            setter.invoke(obj, constraints);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignore for now
            System.out.println("Damn it...");
        } catch (Throwable t) {
            System.out.println("What the heck...");
        }
    }

    /**
     * Updates a {@code javax.mvc.validation.ValidationResult} property. In pseudo-code:
     * <p/>
     * obj = getter.invoke(resource);
     * obj.setViolations(constraints);
     * setter.invoke(resource, obj);
     *
     * @param resource    resource instance.
     * @param getter      getter to be used.
     * @param constraints new set of constraints.
     */
    public static void updateValidationResultProperty(Object resource, Method getter,
                                                      Set<ConstraintViolation<?>> constraints) {
        try {
            final Object obj = getter.invoke(resource);
            Method setViolations;
            try {
                setViolations = obj.getClass().getMethod("setViolations", Set.class);
            } catch (NoSuchMethodException e) {
                setViolations = obj.getClass().getSuperclass().getMethod("setViolations", Set.class);
            }
            setViolations.invoke(obj, constraints);

            final Method setter = getValidationResultSetter(resource);

            if (setter != null) {
                setter.invoke(resource, obj);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignore for now
        }
    }

    /**
     * Determines if a resource has a property of type {@code javax.mvc.validation.ValidationResult}.
     *
     * @param resource resource instance.
     * @return outcome of test.
     */
    public static boolean hasValidationResultProperty(final Object resource) {
        return getValidationResultGetter(resource) != null && getValidationResultSetter(resource) != null;
    }

    /**
     * Returns a getter for {@code javax.mvc.validation.ValidationResult} or {@code null}
     * if one cannot be found.
     *
     * @param resource resource instance.
     * @return getter or {@code null} if not available.
     */
    public static Method getValidationResultGetter(final Object resource) {
        Class<?> clazz = resource.getClass();
        do {
            for (Method m : clazz.getDeclaredMethods()) {
                if (isValidationResultGetter(m)) {
                    return m;
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class);
        return null;
    }

    /**
     * Determines if a method is a getter for {@code javax.mvc.validation.ValidationResult}.
     *
     * @param m method to test.
     * @return outcome of test.
     */
    private static boolean isValidationResultGetter(Method m) {
        return m.getName().startsWith("get")
                && m.getReturnType().getName().equals(VALIDATION_RESULT)
                && Modifier.isPublic(m.getModifiers()) && m.getParameterTypes().length == 0;
    }

    /**
     * Returns a setter for {@code javax.mvc.validation.ValidationResult} or {@code null}
     * if one cannot be found.
     *
     * @param resource resource instance.
     * @return setter or {@code null} if not available.
     */
    public static Method getValidationResultSetter(final Object resource) {
        return getValidationResultSetter(resource.getClass());
    }

    private static Method getValidationResultSetter(final Class<?> resourceClass) {
        Class<?> clazz = resourceClass;
        do {
            for (Method m : clazz.getDeclaredMethods()) {
                if (isValidationResultSetter(m)) {
                    return m;
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class);
        return null;
    }

    /**
     * Determines if a method is a setter for {@code javax.mvc.validation.ValidationResult}.
     * As a CDI initializer method, it must be annotated with {@link javax.inject.Inject}.
     *
     * @param m method to test.
     * @return outcome of test.
     */
    private static boolean isValidationResultSetter(Method m) {
        return m.getName().startsWith("set") && m.getParameterTypes().length == 1
                && m.getParameterTypes()[0].getName().equals(VALIDATION_RESULT)
                && m.getReturnType() == Void.TYPE && Modifier.isPublic(m.getModifiers())
                && m.getAnnotation(Inject.class) != null;
    }
}
