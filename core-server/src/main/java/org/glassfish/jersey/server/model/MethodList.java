/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * Iterable list of methods on a single class with convenience getters for
 * additional method information.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class MethodList implements Iterable<AnnotatedMethod> {

    private AnnotatedMethod[] methods;

    /**
     * Create new method list for a class.
     *
     * The method list contains {@link Class#getMethods() all methods} available
     * on the class.
     *
     * The {@link Method#isBridge() bridge methods} and methods declared directly
     * on the {@link Object} class are filtered out.
     *
     * @param c class from which the method list is created.
     */
    public MethodList(Class<?> c) {
        this(c, false);
    }

    /**
     * Create new method list for a class.
     *
     * The method list contains {@link Class#getMethods() all methods} available
     * on the class or {@link Class#getDeclaredMethods() declared methods} only,
     * depending on the value of the {@code declaredMethods} parameter.
     *
     * The {@link Method#isBridge() bridge methods} and methods declared directly
     * on the {@link Object} class are filtered out.
     *
     * @param c class from which the method list is created.
     * @param declaredMethods if {@code true} only the {@link Class#getDeclaredMethods()
     *     declared methods} will be included in the method list; otherwise
     *     {@link Class#getMethods() all methods} will be listed.
     */
    public MethodList(Class<?> c, boolean declaredMethods) {
        this(declaredMethods ? getAllDeclaredMethods(c) : getMethods(c));
    }

    private static List<Method> getAllDeclaredMethods(Class<?> c) {
        List<Method> l = new ArrayList<>();
        while (c != null && c != Object.class) {
            l.addAll(AccessController.doPrivileged(ReflectionHelper.getDeclaredMethodsPA(c)));
            c = c.getSuperclass();
        }
        return l;
    }

    private static List<Method> getMethods(Class<?> c) {
        return Arrays.asList(c.getMethods());
    }

    /**
     * Create new method list from the given collection of methods.
     *
     * The {@link Method#isBridge() bridge methods} and methods declared directly
     * on the {@link Object} class are filtered out.
     *
     * @param methods methods to be included in the method list.
     */
    public MethodList(Collection<Method> methods) {
        List<AnnotatedMethod> l = new ArrayList<>(methods.size());
        for (Method m : methods) {
            if (!m.isBridge() && m.getDeclaringClass() != Object.class) {
                l.add(new AnnotatedMethod(m));
            }
        }

        this.methods = new AnnotatedMethod[l.size()];
        this.methods = l.toArray(this.methods);
    }

    /**
     * Create new method list from the given array of methods.
     *
     * The {@link Method#isBridge() bridge methods} and methods declared directly
     * on the {@link Object} class are filtered out.
     *
     * @param methods methods to be included in the method list.
     */
    public MethodList(Method... methods) {
        this(Arrays.asList(methods));
    }

    /**
     * Create new method list from the given array of {@link AnnotatedMethod
     * annotated methods}.
     *
     * @param methods methods to be included in the method list.
     */
    public MethodList(AnnotatedMethod... methods) {
        this.methods = methods;
    }

    /**
     * Iterator over the list of {@link AnnotatedMethod annotated methods} contained
     * in this method list.
     *
     * @return method list iterator.
     */
    @Override
    public Iterator<AnnotatedMethod> iterator() {
        return Arrays.asList(methods).iterator();
    }

    /**
     * Get a new sub-list of methods containing all the methods from this method
     * list that are not public.
     *
     * @return new filtered method sub-list.
     */
    public MethodList isNotPublic() {
        return filter(new Filter() {

            @Override
            public boolean keep(AnnotatedMethod m) {
                return !Modifier.isPublic(m.getMethod().getModifiers());
            }
        });
    }

    /**
     * Get a new sub-list of methods containing all the methods from this method
     * list that have the specific number of parameters.
     *
     * @param paramCount number of method parameters.
     * @return new filtered method sub-list.
     */
    public MethodList hasNumParams(final int paramCount) {
        return filter(new Filter() {

            @Override
            public boolean keep(AnnotatedMethod m) {
                return m.getParameterTypes().length == paramCount;
            }
        });
    }

    /**
     * Get a new sub-list of methods containing all the methods from this method
     * list that declare the specified return type.
     *
     * @param returnType method return type.
     * @return new filtered method sub-list.
     */
    public MethodList hasReturnType(final Class<?> returnType) {
        return filter(new Filter() {

            @Override
            public boolean keep(AnnotatedMethod m) {
                return m.getMethod().getReturnType() == returnType;
            }
        });
    }

    /**
     * Get a new sub-list of methods containing all the methods from this method
     * list with a specified method name prefix.
     *
     * @param prefix method name prefix.
     * @return new filtered method sub-list.
     */
    public MethodList nameStartsWith(final String prefix) {
        return filter(new Filter() {

            @Override
            public boolean keep(AnnotatedMethod m) {
                return m.getMethod().getName().startsWith(prefix);
            }
        });
    }

    /**
     * Get a new sub-list of methods containing all the methods from this method
     * list with a specified method-level annotation declared.
     *
     * @param <T> annotation type.
     *
     * @param annotation annotation class.
     * @return new filtered method sub-list.
     */
    public <T extends Annotation> MethodList withAnnotation(final Class<T> annotation) {
        return filter(new Filter() {

            @Override
            public boolean keep(AnnotatedMethod m) {
                return m.getAnnotation(annotation) != null;
            }
        });
    }

    /**
     * Get a new sub-list of methods containing all the methods from this method
     * list with a method-level annotation declared that is itself annotated with
     * a specified meta-annotation.
     *
     * @param <T> meta-annotation type.
     *
     * @param annotation meta-annotation class.
     * @return new filtered method sub-list.
     */
    public <T extends Annotation> MethodList withMetaAnnotation(final Class<T> annotation) {
        return filter(new Filter() {

            @Override
            public boolean keep(AnnotatedMethod m) {
                for (Annotation a : m.getAnnotations()) {
                    if (a.annotationType().getAnnotation(annotation) != null) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Get a new sub-list of methods containing all the methods from this method
     * list without a specified method-level annotation declared.
     *
     * @param <T> annotation type.
     *
     * @param annotation annotation class.
     * @return new filtered method sub-list.
     */
    public <T extends Annotation> MethodList withoutAnnotation(final Class<T> annotation) {
        return filter(new Filter() {

            @Override
            public boolean keep(AnnotatedMethod m) {
                return m.getAnnotation(annotation) == null;
            }
        });
    }

    /**
     * Get a new sub-list of methods containing all the methods from this method
     * list without any method-level annotation declared that would itself be
     * annotated with a specified meta-annotation.
     *
     * @param <T> meta-annotation type.
     *
     * @param annotation meta-annotation class.
     * @return new filtered method sub-list.
     */
    public <T extends Annotation> MethodList withoutMetaAnnotation(final Class<T> annotation) {
        return filter(new Filter() {

            @Override
            public boolean keep(AnnotatedMethod m) {
                for (Annotation a : m.getAnnotations()) {
                    if (a.annotationType().getAnnotation(annotation) != null) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Method list filter.
     *
     * @see MethodList#filter(Filter)
     */
    public interface Filter {

        /**
         * Decide whether the method should remain in the list or should be filtered
         * out.
         *
         * @param method annotated method.
         * @return {@code true} if the method should be kept in the method list,
         *     {@code false} if it should be filtered out.
         */
        boolean keep(AnnotatedMethod method);
    }

    /**
     * Created a new method list containing only the methods supported by the
     * {@link Filter method list filter}.
     *
     * @param filter method list filter.
     *
     * @return new filtered method list.
     */
    public MethodList filter(Filter filter) {
        List<AnnotatedMethod> result = new ArrayList<>();
        for (AnnotatedMethod m : methods) {
            if (filter.keep(m)) {
                result.add(m);
            }
        }
        return new MethodList(result.toArray(new AnnotatedMethod[result.size()]));
    }
}
