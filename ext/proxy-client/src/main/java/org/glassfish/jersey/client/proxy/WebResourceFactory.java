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
// Portions Copyright [2018] [Payara Foundation and/or its affiliates]

package org.glassfish.jersey.client.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.BeanParam;
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
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;

import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.model.Parameter;
import org.glassfish.jersey.client.inject.ParameterInserter;
import org.glassfish.jersey.client.inject.ParameterInserterProvider;

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
    private final InjectionManager injectionManager;
    private final Map<Parameter, ParameterInserter> inserterCache = new HashMap<>();

    private static final MultivaluedMap<String, Object> EMPTY_HEADERS = new MultivaluedHashMap<>();
    private static final Form EMPTY_FORM = new Form();
    private static final Set<Class> PARAM_ANNOTATION_CLASSES = createParamAnnotationSet();
    private static final Logger LOG = Logger.getLogger(WebResourceFactory.class.getName());

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
        return newResource(resourceInterface, target, false, EMPTY_HEADERS, Collections.<Cookie>emptyList(), EMPTY_FORM);
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

        ClientConfig clientConfig = (ClientConfig) target.getConfiguration();
        this.injectionManager = clientConfig.getRuntime().getInjectionManager();
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

        final List<Parameter> parameters = Collections.unmodifiableList(
                Parameter.create(proxyIfc, method.getDeclaringClass(), method, false)
        );

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
        final MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<String, Object>(this.headers);
        final LinkedList<Cookie> cookies = new LinkedList<>(this.cookies);
        final Form form = new Form();
        form.asMap().putAll(this.form.asMap());
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object entity = null;
        Type entityType = null;
        for (int i = 0; i < paramAnnotations.length; i++) {
            final Map<Class, Annotation> annotations = new HashMap<>();
            for (final Annotation annotation : paramAnnotations[i]) {
                annotations.put(annotation.annotationType(), annotation);
            }

            Object value = args[i];
            Parameter parameter = parameters.get(i);
            if (!hasAnyParamAnnotation(annotations)) {
                entityType = method.getGenericParameterTypes()[i];
                entity = value;
            } else {
                newTarget = parseParamMetadata(newTarget, headers, cookies, form, annotations, parameter, value);
            }
        }

        if (httpMethod == null) {
            // the method is a subresource locator
            return WebResourceFactory.newResource(responseType, newTarget, true, headers, cookies, form);
        }

        // determine content type
        String contentType = null;
        final List<Object> contentTypeEntries = headers.get(HttpHeaders.CONTENT_TYPE);
        if (entity != null) {
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

        final Object result;

        if (entity == null && !form.asMap().isEmpty()) {
            entity = form;
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
        } else {
            if (contentType == null) {
                if (entity instanceof javax.json.JsonValue) {
                    contentType = MediaType.APPLICATION_JSON;
                } else {
                    contentType = MediaType.TEXT_PLAIN;
                }
            }
            if (!form.asMap().isEmpty()) {
                if (entity instanceof Form) {
                    ((Form) entity).asMap().putAll(form.asMap());
                } else {
                    // TODO: should at least log some warning here
                }
            }
        }

        // accepted media types
        Produces produces = method.getAnnotation(Produces.class);
        if (produces == null) {
            produces = proxyIfc.getAnnotation(Produces.class);
        }
        final String[] accepts = (produces == null) ? EMPTY : produces.value();

        Invocation.Builder builder = newTarget.request()
                .headers(headers) // this resets all headers so do this first
                .accept(accepts); // if @Produces is defined, propagate values into Accept header; empty array is NO-OP

        for (final Cookie c : cookies) {
            builder = builder.cookie(c);
        }

        final GenericType responseGenericType = new GenericType(method.getGenericReturnType());

        GenericType responseParameterizedType = responseGenericType;
        if (method.getGenericReturnType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            responseParameterizedType = new GenericType(typeArguments[0]);
        }

        if (entity != null) {
            if (entityType instanceof ParameterizedType) {
                entity = new GenericEntity(entity, entityType);
            }
            if (responseType.isAssignableFrom(java.util.concurrent.CompletionStage.class)) {
                result = builder.rx().method(httpMethod, Entity.entity(entity, contentType), responseParameterizedType);
            } else if (responseType.isAssignableFrom(java.util.concurrent.Future.class)) {
                result = builder.async().method(httpMethod, Entity.entity(entity, contentType), responseParameterizedType);
            } else {
                result = builder.method(httpMethod, Entity.entity(entity, contentType), responseGenericType);
            }
        } else {
            if (responseType.isAssignableFrom(java.util.concurrent.CompletionStage.class)) {
                result = builder.rx().method(httpMethod, responseParameterizedType);
            } else if (responseType.isAssignableFrom(java.util.concurrent.Future.class)) {
                result = builder.async().method(httpMethod, responseParameterizedType);
            } else {
                result = builder.method(httpMethod, responseGenericType);
            }
        }

        return result;
    }

    private WebTarget parseParamMetadata(
            WebTarget newTarget,
            MultivaluedHashMap<String, Object> headers,
            LinkedList<Cookie> cookies,
            Form form,
            Map<Class, Annotation> anns,
            Object value) {
        return parseParamMetadata(newTarget, headers, cookies, form, anns, null, value);
    }

    private WebTarget parseParamMetadata(
            WebTarget newTarget,
            MultivaluedHashMap<String, Object> headers,
            LinkedList<Cookie> cookies,
            Form form,
            Map<Class, Annotation> anns,
            Parameter parameter,
            Object value) {

        Annotation ann;
        if (value == null && (ann = anns.get(DefaultValue.class)) != null) {
            value = ((DefaultValue) ann).value();
        }

        if (value != null) {
            ParameterInserter<Object, Object> parameterInserter = getParameterInserter(parameter);

            if ((ann = anns.get(PathParam.class)) != null) {
                if (parameterInserter != null) {
                    value = parameterInserter.insert(value);
                }
                newTarget = newTarget.resolveTemplate(((PathParam) ann).value(), value);
            } else if ((ann = anns.get((QueryParam.class))) != null) {
                if (parameterInserter != null) {
                    value = parameterInserter.insert(value);
                }
                if (value instanceof Collection) {
                    newTarget = newTarget.queryParam(((QueryParam) ann).value(), convert((Collection) value));
                } else {
                    newTarget = newTarget.queryParam(((QueryParam) ann).value(), value);
                }
            } else if (anns.get(BeanParam.class) != null) {
                Class<?> beanParamType = value.getClass();
                Field[] fields = beanParamType.getDeclaredFields();
                for (Field field : fields) {
                    final Map<Class, Annotation> annsIn = new HashMap<>();
                    for (final Annotation annIn : field.getAnnotations()) {
                        annsIn.put(annIn.annotationType(), annIn);
                    }
                    if (hasAnyParamAnnotation(annsIn)) {
                        try {
                            if (!field.isAccessible()) {
                                field.setAccessible(true);
                            }
                            newTarget = parseParamMetadata(newTarget, headers, cookies, form, annsIn, field.get(value));
                        } catch (IllegalArgumentException | IllegalAccessException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                }
                Method[] methods = beanParamType.getDeclaredMethods();
                for (Method method : methods) {
                    final Map<Class, Annotation> annsIn = new HashMap<>();
                    for (final Annotation annIn : method.getAnnotations()) {
                        annsIn.put(annIn.annotationType(), annIn);
                    }
                    if (hasAnyParamAnnotation(annsIn)) {
                        try {
                            if (!method.isAccessible()) {
                                method.setAccessible(true);
                            }
                            newTarget = parseParamMetadata(newTarget, headers, cookies, form, annsIn, method.invoke(value));
                        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } else if ((ann = anns.get((HeaderParam.class))) != null) {
                if (parameterInserter != null) {
                    value = parameterInserter.insert(value);
                }
                if (value instanceof Collection) {
                    headers.addAll(((HeaderParam) ann).value(), convert((Collection) value));
                } else {
                    headers.addAll(((HeaderParam) ann).value(), value);
                }

            } else if ((ann = anns.get((CookieParam.class))) != null) {
                if (parameterInserter != null) {
                    value = parameterInserter.insert(value);
                }
                final String name = ((CookieParam) ann).value();
                Cookie c;
                if (value instanceof Collection) {
                    for (final Object v : ((Collection) value)) {
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
                if (parameterInserter != null) {
                    value = parameterInserter.insert(value);
                }
                if (value instanceof Collection) {
                    newTarget = newTarget.matrixParam(((MatrixParam) ann).value(), convert((Collection) value));
                } else {
                    newTarget = newTarget.matrixParam(((MatrixParam) ann).value(), value);
                }
            } else if ((ann = anns.get((FormParam.class))) != null) {
                if (value instanceof Collection) {
                    for (final Object v : ((Collection) value)) {
                        form.param(((FormParam) ann).value(), v.toString());
                    }
                } else {
                    form.param(((FormParam) ann).value(), value.toString());
                }
            }
        }
        return newTarget;
    }

    private <T, R> ParameterInserter<T, R> getParameterInserter(Parameter parameter) {
        ParameterInserter<T, R> parameterInserter = null;
        if (parameter != null) {
            parameterInserter = inserterCache.get(parameter);
            if (parameterInserter == null) {
                Iterable<ParameterInserterProvider> parameterInserterProviders
                        = Providers.getAllProviders(injectionManager, ParameterInserterProvider.class);
                for (final ParameterInserterProvider parameterInserterProvider : parameterInserterProviders) {
                    if (parameterInserterProvider != null) {
                        parameterInserter = (ParameterInserter<T, R>) parameterInserterProvider.get(parameter);
                        inserterCache.put(parameter, parameterInserter);
                        break;
                    }
                }
            }
        }
        return parameterInserter;
    }

    private boolean hasAnyParamAnnotation(final Map<Class, Annotation> anns) {
        return anns.keySet()
                .stream()
                .anyMatch(PARAM_ANNOTATION_CLASSES::contains);
    }

    private Object[] convert(final Collection value) {
        return value.toArray();
    }

    private static WebTarget addPathFromAnnotation(final AnnotatedElement ae, WebTarget target) {
        final Path path = ae.getAnnotation(Path.class);
        if (path != null && !"/".equals(path.value())) {
            target = target.path(path.value());
        }
        return target;
    }

    private static Set<Class> createParamAnnotationSet() {
        Set<Class> set = new HashSet<>(7);
        set.add(HeaderParam.class);
        set.add(CookieParam.class);
        set.add(MatrixParam.class);
        set.add(QueryParam.class);
        set.add(PathParam.class);
        set.add(FormParam.class);
        set.add(BeanParam.class);
        return Collections.unmodifiableSet(set);
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
