/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * Factory for client-side representation of a resource.
 * See the <a href="package-summary.html">package overview</a>
 * for an example on how to use this class.
 *
 * @author Martin Matula
 */
public final class WebResourceFactory {

    private static final String[] EMPTY = {};

    private final WebTarget target;
    private final MultivaluedMap<String, Object> headers;
    private final List<Cookie> cookies;
    private final Form form;

    private static final MultivaluedMap<String, Object> EMPTY_HEADERS = new MultivaluedHashMap<>();
    private static final Form EMPTY_FORM = new Form();
    private static final List<Class<?>> PARAM_ANNOTATION_CLASSES = Arrays.asList(PathParam.class, QueryParam.class,
            HeaderParam.class, CookieParam.class, MatrixParam.class, FormParam.class);

    /**
     * Creates a new client-side representation of a resource described by
     * the interface passed in the first argument.
     * <p/>
     * Calling this method has the same effect as calling {@code WebResourceFactory.newResource(resourceInterface, rootTarget,
     * false)}.
     *
     * @param <C>               Type of the resource to be created.
     * @param resourceInterface Interface describing the resource to be created.
     * @param target            WebTarget pointing to the resource or the parent of the resource.
     * @return Instance of a class implementing the resource interface that can
     * be used for making requests to the server.
     */
    public static <C> C newResource(final Class<C> resourceInterface, final WebTarget target) {
        return newResource(resourceInterface, target, false, EMPTY_HEADERS, Collections.emptyList(), EMPTY_FORM);
    }

    /**
     * Creates a new client-side representation of a resource described by
     * the interface passed in the first argument.
     *
     * @param <C>                Type of the resource to be created.
     * @param resourceInterface  Interface describing the resource to be created.
     * @param target             WebTarget pointing to the resource or the parent of the resource.
     * @param ignoreResourcePath If set to true, ignores path annotation on the resource interface (this is used when creating
     *                           sub-resources)
     * @param headers            Header params collected from parent resources (used when creating a sub-resource)
     * @param cookies            Cookie params collected from parent resources (used when creating a sub-resource)
     * @param form               Form params collected from parent resources (used when creating a sub-resource)
     * @return Instance of a class implementing the resource interface that can
     * be used for making requests to the server.
     */
    public static <C> C newResource(final Class<C> resourceInterface,
                                    final WebTarget target,
                                    final boolean ignoreResourcePath,
                                    final MultivaluedMap<String, Object> headers,
                                    final List<Cookie> cookies,
                                    final Form form) {

        try {
            return new ByteBuddy().subclass(resourceInterface)
                    .method(ElementMatchers.any())
                    .intercept(
                            MethodDelegation
                                    .to(
                                            new WebResourceFactory(ignoreResourcePath
                                                    ? target
                                                    : addPathFromAnnotation(resourceInterface, target), headers, cookies, form))
                    )
                    .make()
                    .load(getClassLoader(resourceInterface))
                    .getLoaded()
                    .getConstructor()
                    .newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                 | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Gets the {@link ClassLoader} for the given resource class.
     * This method tries to use a {@link PrivilegedAction} for obtaining the class loader, but this is deprecated since Java 17.
     * As a fallback, obtain the {@link ClassLoader} directly via {@link Class#getClassLoader()}.
     *
     * @param clazz The class to get the {@link ClassLoader} for.
     * @param <C>   Type of the resource to be created.
     * @return The {@link ClassLoader} for the given class.
     */
    private static <C> ClassLoader getClassLoader(final Class<C> clazz) {
        try {
            Class<?> accessControllerClass = Class.forName("java.security.AccessController");
            PrivilegedAction<ClassLoader> classLoaderPrivilegedAction = ReflectionHelper.getClassLoaderPA(clazz);
            Method doPrivilegedMethod = accessControllerClass.getMethod("doPrivileged", PrivilegedAction.class);
            return (ClassLoader) doPrivilegedMethod.invoke(null, classLoaderPrivilegedAction);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException exception) {
            return clazz.getClassLoader();
        }
    }

    private WebResourceFactory(final WebTarget target, final MultivaluedMap<String, Object> headers,
                               final List<Cookie> cookies, final Form form) {
        this.target = target;
        this.headers = headers;
        this.cookies = cookies;
        this.form = form;
    }

    @RuntimeType
    public Object invoke(
            @This final Object proxy,
            @Origin final Method method,
            @SuperMethod(nullIfImpossible = true) final Method superMethod,
            @AllArguments final Object[] args) throws Throwable {
        if (args == null && method.getName().equals("toString")) {
            return toString();
        }

        if (args == null && method.getName().equals("hashCode")) {
            //unique instance in the JVM, and no need to override
            return hashCode();
        }

        if (args != null && args.length == 1 && method.getName().equals("equals")) {
            //unique instance in the JVM, and no need to override
            return equals(args[0]);
        }

        if (superMethod != null) {
            return superMethod.invoke(proxy, args);
        }

        // get the interface describing the resource
        final Class<?> proxyIfc = proxy.getClass().getInterfaces()[0];

        // response type
        final Class<?> responseType = method.getReturnType();

        // determine method name
        String httpMethod = getHttpMethodName(method);
        if (httpMethod == null) {
            for (final Annotation ann : method.getAnnotations()) {
                httpMethod = getHttpMethodName(ann.annotationType());
                if (httpMethod != null) {
                    break;
                }
            }
        }

        // create a new UriBuilder appending the @Path attached to the method
        WebTarget newTarget = addPathFromAnnotation(method, target);

        if (httpMethod == null) {
            if (newTarget == target) {
                // no path annotation on the method -> fail
                throw new UnsupportedOperationException("Not a resource method.");
            } else if (!responseType.isInterface()) {
                // the method is a subresource locator, but returns class,
                // not interface - can't help here
                throw new UnsupportedOperationException("Return type not an interface");
            }
        }

        // process method params (build maps of (Path|Form|Cookie|Matrix|Header..)Params
        // and extract entity type
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>(this.headers);
        final LinkedList<Cookie> cookies = new LinkedList<>(this.cookies);
        final Form form = new Form();
        form.asMap().putAll(this.form.asMap());
        final Annotation[][] paramAnns = method.getParameterAnnotations();
        Object entity = null;
        Type entityType = null;
        for (int i = 0; i < paramAnns.length; i++) {
            final Map<Class<?>, Annotation> anns = new HashMap<>();
            for (final Annotation ann : paramAnns[i]) {
                anns.put(ann.annotationType(), ann);
            }
            Annotation ann;
            Object value = args[i];
            if (!hasAnyParamAnnotation(anns)) {
                entityType = method.getGenericParameterTypes()[i];
                entity = value;
            } else {
                if (value == null && (ann = anns.get(DefaultValue.class)) != null) {
                    value = ((DefaultValue) ann).value();
                }

                if (value != null) {
                    if ((ann = anns.get(PathParam.class)) != null) {
                        newTarget = newTarget.resolveTemplate(((PathParam) ann).value(), value);
                    } else if ((ann = anns.get((QueryParam.class))) != null) {
                        if (value instanceof Collection) {
                            newTarget = newTarget.queryParam(((QueryParam) ann).value(), convert((Collection<?>) value));
                        } else {
                            newTarget = newTarget.queryParam(((QueryParam) ann).value(), value);
                        }
                    } else if ((ann = anns.get((HeaderParam.class))) != null) {
                        if (value instanceof Collection) {
                            headers.addAll(((HeaderParam) ann).value(), convert((Collection<?>) value));
                        } else {
                            headers.addAll(((HeaderParam) ann).value(), value);
                        }

                    } else if ((ann = anns.get((CookieParam.class))) != null) {
                        final String name = ((CookieParam) ann).value();
                        Cookie c;
                        if (value instanceof Collection) {
                            for (final Object v : ((Collection<?>) value)) {
                                if (!(v instanceof Cookie)) {
                                    c = new Cookie(name, v.toString());
                                } else {
                                    c = (Cookie) v;
                                    if (!name.equals(((Cookie) v).getName())) {
                                        // is this the right thing to do? or should I fail? or ignore the difference?
                                        c = new Cookie(name, c.getValue(), c.getPath(), c.getDomain(), c.getVersion());
                                    }
                                }
                                cookies.add(c);
                            }
                        } else {
                            if (!(value instanceof Cookie)) {
                                cookies.add(new Cookie(name, value.toString()));
                            } else {
                                c = (Cookie) value;
                                if (!name.equals(((Cookie) value).getName())) {
                                    // is this the right thing to do? or should I fail? or ignore the difference?
                                    cookies.add(new Cookie(name, c.getValue(), c.getPath(), c.getDomain(), c.getVersion()));
                                }
                            }
                        }
                    } else if ((ann = anns.get((MatrixParam.class))) != null) {
                        if (value instanceof Collection) {
                            newTarget = newTarget.matrixParam(((MatrixParam) ann).value(), convert((Collection<?>) value));
                        } else {
                            newTarget = newTarget.matrixParam(((MatrixParam) ann).value(), value);
                        }
                    } else if ((ann = anns.get((FormParam.class))) != null) {
                        if (value instanceof Collection) {
                            for (final Object v : ((Collection<?>) value)) {
                                form.param(((FormParam) ann).value(), v.toString());
                            }
                        } else {
                            form.param(((FormParam) ann).value(), value.toString());
                        }
                    }
                }
            }
        }

        if (httpMethod == null) {
            // the method is a subresource locator
            return WebResourceFactory.newResource(responseType, newTarget, true, headers, cookies, form);
        }

        // accepted media types
        Produces produces = method.getAnnotation(Produces.class);
        if (produces == null) {
            produces = proxyIfc.getAnnotation(Produces.class);
        }
        final String[] accepts = (produces == null) ? EMPTY : produces.value();

        // determine content type
        String contentType = null;
        if (entity != null) {
            final List<Object> contentTypeEntries = headers.get(HttpHeaders.CONTENT_TYPE);
            if ((contentTypeEntries != null) && (!contentTypeEntries.isEmpty())) {
                contentType = contentTypeEntries.get(0).toString();
            } else {
                Consumes consumes = method.getAnnotation(Consumes.class);
                if (consumes == null) {
                    consumes = proxyIfc.getAnnotation(Consumes.class);
                }
                if (consumes != null && consumes.value().length > 0) {
                    contentType = consumes.value()[0];
                }
            }
        }

        boolean isInstrumentedProxyClient = proxy instanceof InstrumentedProxyClient;

        if (isInstrumentedProxyClient) {
            newTarget = ((InstrumentedProxyClient) proxy).instrumentWebTarget(newTarget, method, args);
        }

        Invocation.Builder builder = newTarget.request()
                .headers(headers) // this resets all headers so do this first
                .accept(accepts); // if @Produces is defined, propagate values into Accept header; empty array is NO-OP

        for (final Cookie c : cookies) {
            builder = builder.cookie(c);
        }

        if (isInstrumentedProxyClient) {
            builder = ((InstrumentedProxyClient) proxy).instrumentInvocationBuilder(builder, method, args);
        }

        final Object result;

        if (entity == null && !form.asMap().isEmpty()) {
            entity = form;
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
        } else {
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
            if (!form.asMap().isEmpty()) {
                if (entity instanceof Form) {
                    ((Form) entity).asMap().putAll(form.asMap());
                } else {
                    // TODO: should at least log some warning here
                }
            }
        }

        final GenericType<?> responseGenericType = new GenericType<>(method.getGenericReturnType());
        if (entity != null) {
            if (entityType instanceof ParameterizedType) {
                entity = new GenericEntity<>(entity, entityType);
            }
            result = builder.method(httpMethod, Entity.entity(entity, contentType), responseGenericType);
        } else {
            result = builder.method(httpMethod, responseGenericType);
        }

        return result;
    }

    private boolean hasAnyParamAnnotation(final Map<Class<?>, Annotation> anns) {
        for (final Class<?> paramAnnotationClass : PARAM_ANNOTATION_CLASSES) {
            if (anns.containsKey(paramAnnotationClass)) {
                return true;
            }
        }
        return false;
    }

    private Object[] convert(final Collection<?> value) {
        return value.toArray();
    }

    private static WebTarget addPathFromAnnotation(final AnnotatedElement ae, WebTarget target) {
        final Path p = ae.getAnnotation(Path.class);
        if (p != null) {
            target = target.path(p.value());
        }
        return target;
    }

    @Override
    public String toString() {
        return target.toString();
    }

    private static String getHttpMethodName(final AnnotatedElement ae) {
        final HttpMethod a = ae.getAnnotation(HttpMethod.class);
        return a == null ? null : a.value();
    }
}
