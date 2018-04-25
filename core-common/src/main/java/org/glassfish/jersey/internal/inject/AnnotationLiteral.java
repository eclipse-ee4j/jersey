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

package org.glassfish.jersey.internal.inject;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

/**
 * Supports inline instantiation of annotation type instances.
 * <p>
 * An instance of an annotation type may be obtained by subclassing <tt>AnnotationLiteral</tt>.
 * <pre>
 * public abstract class PayByQualifier
 *       extends AnnotationLiteral&lt;PayBy&gt;
 *       implements PayBy {}
 * </pre>
 * An extension of AnnotationLiteral must do two things:<OL>
 * <LI>Must have the target annotation as its generic type</LI>
 * <LI>Must implement the target type</LI>
 * </OL>
 * In particular, in-line anonymous extensions of AnnotationLiteral will not
 * work because in-line anonymous extensions of AnnotationLiteral cannot implement
 * the target annotation
 *
 * @param <T> the annotation type
 * @author jwells
 */
public abstract class AnnotationLiteral<T extends Annotation> implements Annotation, Serializable {

    private static final long serialVersionUID = -3645430766814376616L;

    private transient Class<T> annotationType;
    private transient Method[] members;

    protected AnnotationLiteral() {
        Class<?> thisClass = this.getClass();

        boolean foundAnnotation = false;
        for (Class<?> iClass : thisClass.getInterfaces()) {
            if (iClass.isAnnotation()) {
                foundAnnotation = true;
                break;
            }
        }

        if (!foundAnnotation) {
            throw new IllegalStateException(
                    "The subclass " + thisClass.getName() + " of AnnotationLiteral must implement an Annotation");
        }
    }

    private static Class<?> getAnnotationLiteralSubclass(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass.equals(AnnotationLiteral.class)) {
            return clazz;
        } else if (superclass.equals(Object.class)) {
            return null;
        } else {
            return getAnnotationLiteralSubclass(superclass);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getTypeParameter(Class<?> annotationLiteralSuperclass) {
        Type type = annotationLiteralSuperclass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getActualTypeArguments().length == 1) {
                return (Class<T>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        return null;
    }

    private static void setAccessible(final AccessibleObject ao) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            ao.setAccessible(true);
            return null;
        });
    }

    private static Object invoke(Method method, Object instance) {
        try {
            if (!method.isAccessible()) {
                setAccessible(method);
            }
            return method.invoke(instance);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(
                    "Error checking value of member method " + method.getName() + " on " + method.getDeclaringClass(), e);
        }
    }

    private Method[] getMembers() {
        if (members == null) {
            members = AccessController.doPrivileged((PrivilegedAction<Method[]>) annotationType()::getDeclaredMethods);

            if (members.length > 0 && !annotationType().isAssignableFrom(this.getClass())) {
                throw new RuntimeException(
                        getClass() + " does not implement the annotation type with members " + annotationType().getName());
            }
        }
        return members;
    }

    /**
     * Method returns the type of the annotation literal. The value is resolved lazily during the first call of this method.
     *
     * @return annotation type of this literal.
     */
    public Class<? extends Annotation> annotationType() {
        if (annotationType == null) {
            Class<?> annotationLiteralSubclass = getAnnotationLiteralSubclass(this.getClass());
            if (annotationLiteralSubclass == null) {
                throw new RuntimeException(getClass() + "is not a subclass of AnnotationLiteral");
            }
            annotationType = getTypeParameter(annotationLiteralSubclass);
            if (annotationType == null) {
                throw new RuntimeException(getClass() + " does not specify the type parameter T of AnnotationLiteral<T>");
            }
        }
        return annotationType;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Annotation) {
            Annotation that = (Annotation) other;
            if (this.annotationType().equals(that.annotationType())) {
                for (Method member : getMembers()) {
                    Object thisValue = invoke(member, this);
                    Object thatValue = invoke(member, that);
                    if (thisValue instanceof byte[] && thatValue instanceof byte[]) {
                        if (!Arrays.equals((byte[]) thisValue, (byte[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof short[] && thatValue instanceof short[]) {
                        if (!Arrays.equals((short[]) thisValue, (short[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof int[] && thatValue instanceof int[]) {
                        if (!Arrays.equals((int[]) thisValue, (int[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof long[] && thatValue instanceof long[]) {
                        if (!Arrays.equals((long[]) thisValue, (long[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof float[] && thatValue instanceof float[]) {
                        if (!Arrays.equals((float[]) thisValue, (float[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof double[] && thatValue instanceof double[]) {
                        if (!Arrays.equals((double[]) thisValue, (double[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof char[] && thatValue instanceof char[]) {
                        if (!Arrays.equals((char[]) thisValue, (char[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof boolean[] && thatValue instanceof boolean[]) {
                        if (!Arrays.equals((boolean[]) thisValue, (boolean[]) thatValue)) {
                            return false;
                        }
                    } else if (thisValue instanceof Object[] && thatValue instanceof Object[]) {
                        if (!Arrays.equals((Object[]) thisValue, (Object[]) thatValue)) {
                            return false;
                        }
                    } else {
                        if (!thisValue.equals(thatValue)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {

        int hashCode = 0;
        for (Method member : getMembers()) {
            int memberNameHashCode = 127 * member.getName().hashCode();
            Object value = invoke(member, this);
            int memberValueHashCode;
            if (value instanceof boolean[]) {
                memberValueHashCode = Arrays.hashCode((boolean[]) value);
            } else if (value instanceof short[]) {
                memberValueHashCode = Arrays.hashCode((short[]) value);
            } else if (value instanceof int[]) {
                memberValueHashCode = Arrays.hashCode((int[]) value);
            } else if (value instanceof long[]) {
                memberValueHashCode = Arrays.hashCode((long[]) value);
            } else if (value instanceof float[]) {
                memberValueHashCode = Arrays.hashCode((float[]) value);
            } else if (value instanceof double[]) {
                memberValueHashCode = Arrays.hashCode((double[]) value);
            } else if (value instanceof byte[]) {
                memberValueHashCode = Arrays.hashCode((byte[]) value);
            } else if (value instanceof char[]) {
                memberValueHashCode = Arrays.hashCode((char[]) value);
            } else if (value instanceof Object[]) {
                memberValueHashCode = Arrays.hashCode((Object[]) value);
            } else {
                memberValueHashCode = value.hashCode();
            }
            hashCode += memberNameHashCode ^ memberValueHashCode;
        }
        return hashCode;
    }

}
