/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * Factory for client-side representation of a resource.
 * See the <a href="package-summary.html">package overview</a>
 * for an example on how to use this class.
 *
 * @author Martin Matula
 */
public final class WebResourceFactory implements InvocationHandler {

    private static final String[] EMPTY = {};

    private final WebTarget target;
    private final MultivaluedMap<String, Object> headers;
    private final List<Cookie> cookies;
    private final Form form;

    private static final MultivaluedMap<String, Object> EMPTY_HEADERS = new MultivaluedHashMap<>();
    private static final Form EMPTY_FORM = new Form();
    private static final List<Class<?>> PARAM_ANNOTATION_CLASSES = Arrays.asList(PathParam.class, QueryParam.class,
            HeaderParam.class, CookieParam.class, MatrixParam.class, FormParam.class, BeanParam.class);

    /**
     * Creates a new client-side representation of a resource described by
     * the interface passed in the first argument.
     * <p/>
     * Calling this method has the same effect as calling {@code WebResourceFactory.newResource(resourceInterface, rootTarget,
     *false)}.
     *
     * @param <C> Type of the resource to be created.
     * @param resourceInterface Interface describing the resource to be created.
     * @param target WebTarget pointing to the resource or the parent of the resource.
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
     * @param <C> Type of the resource to be created.
     * @param resourceInterface Interface describing the resource to be created.
     * @param target WebTarget pointing to the resource or the parent of the resource.
     * @param ignoreResourcePath If set to true, ignores path annotation on the resource interface (this is used when creating
     * sub-resources)
     * @param headers Header params collected from parent resources (used when creating a sub-resource)
     * @param cookies Cookie params collected from parent resources (used when creating a sub-resource)
     * @param form Form params collected from parent resources (used when creating a sub-resource)
     * @return Instance of a class implementing the resource interface that can
     * be used for making requests to the server.
     */
    @SuppressWarnings("unchecked")
    public static <C> C newResource(final Class<C> resourceInterface,
                                    final WebTarget target,
                                    final boolean ignoreResourcePath,
                                    final MultivaluedMap<String, Object> headers,
                                    final List<Cookie> cookies,
                                    final Form form) {

        return (C) Proxy.newProxyInstance(AccessController.doPrivileged(ReflectionHelper.getClassLoaderPA(resourceInterface)),
                new Class[] {resourceInterface},
                new WebResourceFactory(ignoreResourcePath ? target : addPathFromAnnotation(resourceInterface, target),
                        headers, cookies, form));
    }

    private WebResourceFactory(final WebTarget target, final MultivaluedMap<String, Object> headers,
                               final List<Cookie> cookies, final Form form) {
        this.target = target;
        this.headers = headers;
        this.cookies = cookies;
        this.form = form;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
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
        RequestParameters requestParameters = new RequestParameters(newTarget, headers, cookies, form);
        final Annotation[][] paramAnns = method.getParameterAnnotations();
        Object entity = null;
        Type entityType = null;
        for (int i = 0; i < paramAnns.length; i++) {
            final Map<Class<?>, Annotation> anns = new HashMap<>();
            for (final Annotation ann : paramAnns[i]) {
                anns.put(ann.annotationType(), ann);
            }
            Object value = args[i];
            if (!RequestParameters.hasAnyParamAnnotation(anns)) {
                entityType = method.getGenericParameterTypes()[i];
                entity = value;
            } else {
                Annotation ann;
                if (value == null && (ann = anns.get(DefaultValue.class)) != null) {
                    value = ((DefaultValue) ann).value();
                }
                if (value != null) {
                    requestParameters.addParameter(value, anns);
                }
            }
        }
        newTarget = requestParameters.getNewTarget();

        if (httpMethod == null) {
            // the method is a subresource locator
            return WebResourceFactory.newResource(responseType, newTarget, true,
                    requestParameters.getHeaders(), requestParameters.getCookies(), requestParameters.getForm());
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
            final List<Object> contentTypeEntries = requestParameters.getHeaders().get(HttpHeaders.CONTENT_TYPE);
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

        Invocation.Builder builder = newTarget.request()
                .headers(requestParameters.getHeaders()) // this resets all headers so do this first
                .accept(accepts); // if @Produces is defined, propagate values into Accept header; empty array is NO-OP

        for (final Cookie c : requestParameters.getCookies()) {
            builder = builder.cookie(c);
        }

        final Object result;

        if (entity == null && !requestParameters.getForm().asMap().isEmpty()) {
            entity = requestParameters.getForm();
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
        } else {
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }
            if (!requestParameters.getForm().asMap().isEmpty()) {
                if (entity instanceof Form) {
                    ((Form) entity).asMap().putAll(requestParameters.getForm().asMap());
                } else {
                    // TODO: should at least log some warning here
                }
            }
        }

        final GenericType<?> responseGenericType = new GenericType(method.getGenericReturnType());
        if (entity != null) {
            if (entityType instanceof ParameterizedType) {
                entity = new GenericEntity(entity, entityType);
            }
            result = builder.method(httpMethod, Entity.entity(entity, contentType), responseGenericType);
        } else {
            result = builder.method(httpMethod, responseGenericType);
        }

        return result;
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
