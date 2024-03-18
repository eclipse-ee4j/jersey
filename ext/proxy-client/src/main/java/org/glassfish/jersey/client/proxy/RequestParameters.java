/*
 * Copyright (c) 2012, 2024 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Collector to retrieve parameters for setting up the HTTP request sent in the invoke method of WebResourceFactory
 * The addParameter method takes a single annotated method parameter or annotated field or property of a BeanParam
 * and adds the information to the web target, headers, cookie list or form.
 */
class RequestParameters {

    private WebTarget newTarget;
    private final MultivaluedHashMap<String, Object> headers;
    private final LinkedList<Cookie> cookies;
    private final Form form;

    private static final List<Class<?>> PARAM_ANNOTATION_CLASSES = Arrays.asList(PathParam.class, QueryParam.class,
            HeaderParam.class, CookieParam.class, MatrixParam.class, FormParam.class, BeanParam.class);

    RequestParameters(final WebTarget newTarget, final MultivaluedMap<String, Object> headers,
                             final List<Cookie> cookies, final Form form) {

        this.headers = new MultivaluedHashMap<String, Object>(headers);
        this.cookies = new LinkedList<>(cookies);
        this.form = new Form();
        this.form.asMap().putAll(form.asMap());

        this.newTarget = newTarget;
    }

    void addParameter(final Object value, final Map<Class<?>, Annotation> anns)
            throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        Annotation ann;
        if ((ann = anns.get(PathParam.class)) != null) {
            newTarget = newTarget.resolveTemplate(((PathParam) ann).value(), value);
        } else if ((ann = anns.get((QueryParam.class))) != null) {
            if (value instanceof Collection) {
                newTarget = newTarget.queryParam(((QueryParam) ann).value(), convert((Collection<?>) value, true));
            } else {
                newTarget = newTarget.queryParam(((QueryParam) ann).value(), encodeTemplate(value));
            }
        } else if ((ann = anns.get((HeaderParam.class))) != null) {
            if (value instanceof Collection) {
                headers.addAll(((HeaderParam) ann).value(), convert((Collection<?>) value, false));
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
                newTarget = newTarget.matrixParam(((MatrixParam) ann).value(), convert((Collection<?>) value, true));
            } else {
                newTarget = newTarget.matrixParam(((MatrixParam) ann).value(), encodeTemplate(value));
            }
        } else if ((ann = anns.get((FormParam.class))) != null) {
            if (value instanceof Collection) {
                for (final Object v : ((Collection<?>) value)) {
                    form.param(((FormParam) ann).value(), v.toString());
                }
            } else {
                form.param(((FormParam) ann).value(), value.toString());
            }
        } else if ((anns.get((BeanParam.class))) != null) {
            if (value instanceof Collection) {
                for (final Object v : ((Collection<?>) value)) {
                    addBeanParameter(v);
                }
            } else {
                addBeanParameter(value);
            }
        }
    }

    private void addBeanParameter(final Object beanParam)
            throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        Class<?> beanClass = beanParam.getClass();
        List<Field> fields = new ArrayList<>();
        getAllNonStaticFields(fields, beanClass);

        for (final Field field : fields) {
            Object value = null;
            final Map<Class<?>, Annotation> anns = new HashMap<>();

            // get field annotations
            for (final Annotation ann : field.getAnnotations()) {
                anns.put(ann.annotationType(), ann);
            }

            if (field.canAccess(beanParam) && hasAnyParamAnnotation(anns)) {
                value = field.get(beanParam);
            } else {
                // get getter annotations if there are no field annotations
                for (final PropertyDescriptor pd : Introspector.getBeanInfo(beanClass).getPropertyDescriptors()) {
                    if (pd.getName().equals(field.getName()) && pd.getReadMethod() != null) {
                        for (final Annotation ann : pd.getReadMethod().getAnnotations()) {
                            anns.put(ann.annotationType(), ann);
                        }
                        if (hasAnyParamAnnotation(anns)) {
                            value = pd.getReadMethod().invoke(beanParam);
                        }
                    }
                }
            }

            if (value != null) {
                addParameter(value, anns);
            }
        }
    }

    private List<Field> getAllNonStaticFields(List<Field> fields, Class<?> type) {

        List<Field> nonStaticFields = Arrays.stream(type.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .collect(Collectors.toList());
        fields.addAll(nonStaticFields);

        if (type.getSuperclass() != null) {
            getAllNonStaticFields(fields, type.getSuperclass());
        }

        return fields;
    }

    private Object[] convert(Collection<?> value, boolean encode) {
        Object[] array = new Object[value.size()];
        int index = 0;
        for (Iterator<?> it = value.iterator(); it.hasNext();) {
            Object o = it.next();
            array[index++] = o == null ? o : (encode ? encodeTemplate(o) : o.toString());
        }
        return array;
    }

    /**
     * The Query and Matrix arguments are never templates
     * @param notNull an Object that is not null
     * @return encoded curly brackets within the string representation of the {@code notNull}
     */
    private String encodeTemplate(Object notNull) {
        return notNull.toString().replace("{", "%7B").replace("}", "%7D");
    }

    public static boolean hasAnyParamAnnotation(final Map<Class<?>, Annotation> anns) {
        for (final Class<?> paramAnnotationClass : PARAM_ANNOTATION_CLASSES) {
            if (anns.containsKey(paramAnnotationClass)) {
                return true;
            }
        }
        return false;
    }

    WebTarget getNewTarget() {
        return newTarget;
    }

    MultivaluedHashMap<String, Object> getHeaders() {
        return headers;
    }

    LinkedList<Cookie> getCookies() {
        return cookies;
    }

    Form getForm() {
        return form;
    }

}
