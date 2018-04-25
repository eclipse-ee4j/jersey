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

package org.glassfish.jersey.message.filtering.spi;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBElement;

import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.DataStructures;

/**
 * SPI utility methods for entity filtering.
 *
 * @author Michal Gajdos
 */
public final class FilteringHelper {

    /**
     * Empty annotation array.
     */
    public static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    private static final ConcurrentMap<Type, Class<?>> ENTITY_CLASSES = DataStructures.createConcurrentMap();

    /**
     * Determine whether given class is filterable by entity-filtering. Filterable classes are all classes that are not primitives
     * and are not in a package matching <code>java.*</code>.
     *
     * @param clazz entity class to be examined.
     * @return {@code true} whether the class is filterable, {@code false otherwise}.
     */
    public static boolean filterableEntityClass(final Class<?> clazz) {
        return !ReflectionHelper.isPrimitive(clazz) && !clazz.getPackage().getName().startsWith("java.");
    }

    /**
     * A convenience method to get the domain class (i.e. <i>Customer</i>) from the generic type (i.e. <i>Customer</i>,
     * <i>List&lt;Customer></i>, <i>JAXBElement&lt;Customer></i>, <i>JAXBElement&lt;? extends Customer></i>,
     * <i>List&lt;JAXBElement&lt;Customer>></i>, or <i>List&lt;JAXBElement&lt;? extends Customer>></i>).
     *
     * @param genericType type to obtain entity domain class for.
     * @return entity domain class.
     */
    public static Class<?> getEntityClass(final Type genericType) {
        if (!ENTITY_CLASSES.containsKey(genericType)) {
            ENTITY_CLASSES.putIfAbsent(genericType, _getEntityClass(genericType));
        }
        return ENTITY_CLASSES.get(genericType);
    }

    /**
     * Note: This method was copied from {@code MOXyJsonProvider}.
     */
    @SuppressWarnings("JavaDoc")
    private static Class<?> _getEntityClass(final Type genericType) {
        if (null == genericType) {
            return Object.class;
        }
        if (genericType instanceof Class && genericType != JAXBElement.class) {
            final Class<?> clazz = (Class<?>) genericType;
            if (clazz.isArray()) {
                return _getEntityClass(clazz.getComponentType());
            }
            return clazz;
        } else if (genericType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) genericType;
            final Type[] arguments = parameterizedType.getActualTypeArguments();

            final Type type = parameterizedType.getRawType() == Map.class ? arguments[1] : arguments[0];
            if (type instanceof ParameterizedType) {
                final Type rawType = ((ParameterizedType) type).getRawType();
                if (rawType == JAXBElement.class) {
                    return _getEntityClass(type);
                }
            } else if (type instanceof WildcardType) {
                final Type[] upperTypes = ((WildcardType) type).getUpperBounds();
                if (upperTypes.length > 0) {
                    final Type upperType = upperTypes[0];
                    if (upperType instanceof Class) {
                        return (Class<?>) upperType;
                    }
                }
            } else if (JAXBElement.class == type
                    || type instanceof TypeVariable) {
                return Object.class;
            }
            //noinspection ConstantConditions
            return (Class<?>) type;
        } else if (genericType instanceof GenericArrayType) {
            final GenericArrayType genericArrayType = (GenericArrayType) genericType;
            return _getEntityClass(genericArrayType.getGenericComponentType());
        } else {
            return Object.class;
        }
    }

    /**
     * Get accessor method mappings (field -> getter/setter) for given class.
     *
     * @param clazz entity class to obtain property methods for.
     * @param isGetter flag determining whether to look for getters or setters.
     * @return non-null map of field-accessor mappings.
     */
    public static Map<String, Method> getPropertyMethods(final Class<?> clazz, final boolean isGetter) {
        final Map<String, Method> methods = new HashMap<>();

        for (final Method method : AccessController.doPrivileged(ReflectionHelper.getDeclaredMethodsPA(clazz))) {
            if ((isGetter && ReflectionHelper.isGetter(method))
                    || (!isGetter && ReflectionHelper.isSetter(method))) {

                methods.put(ReflectionHelper.getPropertyName(method), method);
            }
        }

        final Class<?> parent = clazz.getSuperclass();
        // We're interested in fields/accessors in superclasses but not those from i.e. Object/Enum.
        if (parent != null && !parent.getPackage().getName().startsWith("java.lang")) {
            methods.putAll(getPropertyMethods(parent, isGetter));
        }

        return methods;
    }

    /**
     * Get a set containing default filtering scope.
     *
     * @return default filtering scope.
     */
    public static Set<String> getDefaultFilteringScope() {
        return Collections.singleton(ScopeProvider.DEFAULT_SCOPE);
    }

    /**
     * Prevent instantiation.
     */
    private FilteringHelper() {
    }
}
