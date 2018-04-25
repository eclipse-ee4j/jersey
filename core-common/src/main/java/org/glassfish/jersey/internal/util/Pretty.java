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

package org.glassfish.jersey.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Utility class to print the variety of types, useful in {@code toString} method.
 *
 * @author John Wells (john.wells at oracle.com)
 */
public class Pretty {

    private static final String DOT = ".";
    private static final String NULL_STRING = "null";
    private static final String CONSTRUCTOR_NAME = "<init>";

    /**
     * Private constructor to forbid the creation of Pretty instance.
     */
    private Pretty() {
    }

    /**
     * Makes a nice, pretty class (without the package name).
     *
     * @param clazz Make me a pretty class.
     * @return A nice string of the class, with no package.
     */
    public static String clazz(Class<?> clazz) {
        if (clazz == null) {
            return NULL_STRING;
        }

        String cn = clazz.getName();

        int index = cn.lastIndexOf(DOT);
        if (index < 0) {
            return cn;
        }

        // If this fails, the class name somehow ends in dot, which should be illegal
        return cn.substring(index + 1);
    }

    /**
     * Prints a pretty parameterized type.
     *
     * @param pType The parameterized type to print. May not be null.
     * @return A pretty string version of the parameterized type.
     */
    public static String pType(ParameterizedType pType) {
        StringBuffer sb = new StringBuffer();

        sb.append(clazz(ReflectionHelper.getRawClass(pType)) + "<");

        boolean first = true;
        for (Type t : pType.getActualTypeArguments()) {
            if (first) {
                first = false;
                sb.append(type(t));
            } else {
                sb.append("," + type(t));
            }
        }

        sb.append(">");

        return sb.toString();
    }

    /**
     * Returns a pretty string for the given type.
     *
     * @param t A possibly null type.
     * @return A pretty string representing the type.
     */
    public static String type(Type t) {
        if (t == null) {
            return NULL_STRING;
        }

        if (t instanceof Class) {
            return clazz((Class<?>) t);
        }

        if (t instanceof ParameterizedType) {
            return pType((ParameterizedType) t);
        }

        return t.toString();
    }

    /**
     * Make a nice pretty string out of the constructor and all its parameters.
     *
     * @param constructor The constructor to make pretty.
     * @return A nice pretty string.
     */
    public static String constructor(Constructor<?> constructor) {
        if (constructor == null) {
            return NULL_STRING;
        }

        return CONSTRUCTOR_NAME + prettyPrintParameters(constructor.getParameterTypes());
    }

    /**
     * Makes a nice pretty string of the method, with the method name and all parameters.
     *
     * @param method The method to make pretty.
     * @return A nice pretty string.
     */
    public static String method(Method method) {
        if (method == null) {
            return NULL_STRING;
        }

        return method.getName() + prettyPrintParameters(method.getParameterTypes());
    }

    /**
     * Returns a pretty string representing a Field.
     *
     * @param field The possibly null field.
     * @return A pretty string representing the field.
     */
    public static String field(Field field) {
        if (field == null) {
            return NULL_STRING;
        }

        Type t = field.getGenericType();

        String baseString;
        if (t instanceof Class) {
            baseString = clazz((Class<?>) t);
        } else {
            baseString = type(t);
        }

        return "field(" + baseString + " " + field.getName() + " in " + field.getDeclaringClass().getName() + ")";
    }

    /**
     * Returns a pretty string for the given array.
     *
     * @param array The possibly null array to represent.
     * @return A pretty string representation of the array.
     */
    public static String array(Object[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        StringBuffer sb = new StringBuffer("{");

        boolean first = true;
        for (Object item : array) {
            if (item != null && (item instanceof Class)) {
                item = Pretty.clazz((Class<?>) item);
            }

            if (first) {
                first = false;

                sb.append((item == null) ? "null" : item.toString());
            } else {
                sb.append("," + ((item == null) ? "null" : item.toString()));
            }
        }

        sb.append("}");

        return sb.toString();
    }

    /**
     * Returns a pretty string representing the collection.
     *
     * @param collection A possibly null collection to represent.
     * @return A pretty string representing the collection.
     */
    public static String collection(Collection<?> collection) {
        if (collection == null) {
            return NULL_STRING;
        }
        return array(collection.toArray(new Object[collection.size()]));
    }

    private static String prettyPrintParameters(Class<?> params[]) {
        if (params == null) {
            return NULL_STRING;
        }

        StringBuffer sb = new StringBuffer("(");

        boolean first = true;
        for (Class<?> param : params) {
            if (first) {
                sb.append(clazz(param));
                first = false;
            } else {
                sb.append("," + clazz(param));
            }
        }

        sb.append(")");

        return sb.toString();
    }

}
