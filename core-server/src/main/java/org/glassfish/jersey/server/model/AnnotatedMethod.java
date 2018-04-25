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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * Annotated method representation.
 *
 * @author Paul Sandoz
 */
public final class AnnotatedMethod implements AnnotatedElement {

    @SuppressWarnings("unchecked")
    private static final Set<Class<? extends Annotation>> METHOD_META_ANNOTATIONS = getSet(
            HttpMethod.class);
    @SuppressWarnings("unchecked")
    private static final Set<Class<? extends Annotation>> METHOD_ANNOTATIONS = getSet(
            Path.class,
            Produces.class,
            Consumes.class);
    @SuppressWarnings("unchecked")
    private static final Set<Class<? extends Annotation>> PARAMETER_ANNOTATIONS = getSet(
            Context.class,
            Encoded.class,
            DefaultValue.class,
            MatrixParam.class,
            QueryParam.class,
            CookieParam.class,
            HeaderParam.class,
            PathParam.class,
            FormParam.class);

    @SafeVarargs
    private static Set<Class<? extends Annotation>> getSet(final Class<? extends Annotation>... cs) {
        final Set<Class<? extends Annotation>> s = new HashSet<>();
        s.addAll(Arrays.asList(cs));
        return s;
    }

    private final Method m;
    private final Method am;
    private final Annotation[] methodAnnotations;
    private final Annotation[][] parameterAnnotations;

    /**
     * Create annotated method instance from the {@link Method Java method}.
     *
     * @param method Java method.
     */
    public AnnotatedMethod(final Method method) {
        this.m = method;
        this.am = findAnnotatedMethod(method);

        if (method.equals(am)) {
            methodAnnotations = method.getAnnotations();
            parameterAnnotations = method.getParameterAnnotations();
        } else {
            methodAnnotations = mergeMethodAnnotations(method, am);
            parameterAnnotations = mergeParameterAnnotations(method, am);
        }
    }

    /**
     * Get the underlying Java method.
     *
     * @return the underlying Java method.
     */
    public Method getMethod() {
        return am;
    }

    /**
     * Get the underlying declared Java method. This method overrides or is the same as the one retrieved by {@code getMethod}.
     *
     * @return the underlying declared Java method.
     */
    Method getDeclaredMethod() {
        return m;
    }

    /**
     * Get method parameter annotations.
     *
     * @return method parameter annotations.
     */
    public Annotation[][] getParameterAnnotations() {
        return parameterAnnotations.clone();
    }

    /**
     * Get method parameter types.
     *
     * See also {@link Method#getParameterTypes()}.
     *
     * @return method parameter types.
     */
    public Class<?>[] getParameterTypes() {
        return am.getParameterTypes();
    }

    /**
     * Get method type parameters.
     *
     * See also {@link Method#getTypeParameters()}.
     *
     * @return method type parameters.
     */
    @SuppressWarnings("UnusedDeclaration")
    public TypeVariable<Method>[] getTypeParameters() {
        return am.getTypeParameters();
    }

    /**
     * Get generic method parameter types.
     *
     * See also {@link Method#getGenericParameterTypes()}.
     *
     * @return generic method parameter types.
     */
    public Type[] getGenericParameterTypes() {
        return am.getGenericParameterTypes();
    }

    /**
     * Get all instances of the specified meta-annotation type found on the method
     * annotations.
     *
     * @param <T>        meta-annotation type.
     * @param annotation meta-annotation class to be searched for.
     * @return meta-annotation instances of a given type annotating the method
     *         annotations.
     */
    public <T extends Annotation> List<T> getMetaMethodAnnotations(
            final Class<T> annotation) {
        final List<T> ma = new ArrayList<>();
        for (final Annotation a : methodAnnotations) {
            final T metaAnnotation = a.annotationType().getAnnotation(annotation);
            if (metaAnnotation != null) {
                ma.add(metaAnnotation);
            }
        }

        return ma;
    }

    @Override
    public String toString() {
        return m.toString();
    }

    // AnnotatedElement
    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
        for (final Annotation ma : methodAnnotations) {
            if (ma.annotationType() == annotationType) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationType) {
        for (final Annotation ma : methodAnnotations) {
            if (ma.annotationType() == annotationType) {
                return annotationType.cast(ma);
            }
        }
        return am.getAnnotation(annotationType);
    }

    @Override
    public Annotation[] getAnnotations() {
        return methodAnnotations.clone();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getAnnotations();
    }

    private static Annotation[] mergeMethodAnnotations(final Method m, final Method am) {
        final List<Annotation> al = asList(m.getAnnotations());
        for (final Annotation a : am.getAnnotations()) {
            if (!m.isAnnotationPresent(a.getClass())) {
                al.add(a);
            }
        }

        return al.toArray(new Annotation[al.size()]);
    }

    private static Annotation[][] mergeParameterAnnotations(final Method m, final Method am) {
        final Annotation[][] methodParamAnnotations = m.getParameterAnnotations();
        final Annotation[][] annotatedMethodParamAnnotations = am.getParameterAnnotations();

        final List<List<Annotation>> methodParamAnnotationsList = new ArrayList<>();
        for (int i = 0; i < methodParamAnnotations.length; i++) {
            final List<Annotation> al = asList(methodParamAnnotations[i]);
            for (final Annotation a : annotatedMethodParamAnnotations[i]) {
                if (annotationNotInList(a.getClass(), al)) {
                    al.add(a);
                }
            }
            methodParamAnnotationsList.add(al);
        }

        final Annotation[][] mergedAnnotations = new Annotation[methodParamAnnotations.length][];
        for (int i = 0; i < methodParamAnnotations.length; i++) {
            final List<Annotation> paramAnnotations = methodParamAnnotationsList.get(i);
            mergedAnnotations[i] = paramAnnotations.toArray(new Annotation[paramAnnotations.size()]);
        }

        return mergedAnnotations;
    }

    private static boolean annotationNotInList(final Class<? extends Annotation> ca, final List<Annotation> la) {
        for (final Annotation a : la) {
            if (ca == a.getClass()) {
                return false;
            }
        }
        return true;
    }

    private static Method findAnnotatedMethod(final Method m) {
        final Method am = findAnnotatedMethod(m.getDeclaringClass(), m);
        return (am != null) ? am : m;
    }

    private static Method findAnnotatedMethod(final Class<?> c, Method m) {
        if (c == Object.class) {
            return null;
        }

        m = AccessController.doPrivileged(ReflectionHelper.findMethodOnClassPA(c, m));
        if (m == null) {
            return null;
        }

        if (hasAnnotations(m)) {
            return m;
        }

        // Super classes take precedence over interfaces
        final Class<?> sc = c.getSuperclass();
        if (sc != null && sc != Object.class) {
            final Method sm = findAnnotatedMethod(sc, m);
            if (sm != null) {
                return sm;
            }
        }

        for (final Class<?> ic : c.getInterfaces()) {
            final Method im = findAnnotatedMethod(ic, m);
            if (im != null) {
                return im;
            }
        }

        return null;
    }

    private static boolean hasAnnotations(final Method m) {
        return hasMetaMethodAnnotations(m)
                || hasMethodAnnotations(m)
                || hasParameterAnnotations(m);
    }

    private static boolean hasMetaMethodAnnotations(final Method m) {
        for (final Class<? extends Annotation> ac : METHOD_META_ANNOTATIONS) {
            for (final Annotation a : m.getAnnotations()) {
                if (a.annotationType().getAnnotation(ac) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hasMethodAnnotations(final Method m) {
        for (final Class<? extends Annotation> ac : METHOD_ANNOTATIONS) {
            if (m.isAnnotationPresent(ac)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasParameterAnnotations(final Method m) {
        for (final Annotation[] as : m.getParameterAnnotations()) {
            for (final Annotation a : as) {
                if (PARAMETER_ANNOTATIONS.contains(a.annotationType())) {
                    return true;
                }
            }
        }

        return false;
    }

    @SafeVarargs
    private static <T> List<T> asList(final T... ts) {
        final List<T> l = new ArrayList<>();
        l.addAll(Arrays.asList(ts));
        return l;
    }
}
