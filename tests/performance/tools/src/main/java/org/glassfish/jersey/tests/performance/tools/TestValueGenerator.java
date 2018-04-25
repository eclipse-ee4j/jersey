/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.tools;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Abstract class for test data generation.
 *
 * <p>Creates the pattern for different generation strategies.
 * Contains the (limited) logic for class graph walk-through.<p/>
 * <p>Every field which should be populated must be annotated by
 * {@link org.glassfish.jersey.tests.performance.tools.GenerateForTest}</p>
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public abstract class TestValueGenerator {

    private static final int MAX_RECURSION_LEVEL = 5;

    private static final Logger log = Logger.getLogger(TestValueGenerator.class.getName());

    /** returns testing data int value */
    public abstract int getInt();

    /** returns testing data char value */
    public abstract char getChar();

    /** returns testing data String value */
    public abstract String getString();

    /** returns testing data long value */
    public abstract long getLong();

    /** returns testing data float value */
    public abstract float getFloat();

    /** returns testing data double value */
    public abstract double getDouble();

    /** returns testing data byte value */
    public abstract byte getByte();

    /** returns testing data short value */
    public abstract short getShort();

    /** returns testing data boolean value */
    public abstract boolean getBoolean();

    /** returns testing data enum value */
    public abstract <T> T getEnum(Class<T> enumType);

    protected Object handlePrimitivesAndWrappers(Class<?> type) {
        if (type.isAssignableFrom(String.class)) {
            return getString();
        }
        if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
            return getInt();
        }
        if (type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class)) {
            return getChar();
        }
        if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
            return getFloat();
        }
        if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
            return getLong();
        }
        if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
            return getDouble();
        }
        if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
            return getByte();
        }
        if (type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
            return getShort();
        }
        if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
            return getBoolean();
        }
        return null;
    }

    protected Object handleCollections(Class<?> type,
                                       GenerateForTest annotation,
                                       int recursionLevel) throws ReflectiveOperationException {
        int testDataLength = annotation.length();
        Class<?> collectionMemberType = annotation.collectionMemberType();
        Class<?> collectionType = type;
        if (collectionType.isInterface()) {
            collectionType = annotation.implementingClass();
            if (collectionType.equals(Object.class)) {
                throw new IllegalArgumentException("Unable to instantiate collection - interface was used for the "
                        + "declaration and parameter 'implementingClass' not set.");
            }
        }
        Object collection = collectionType.newInstance();
        for (int i = 0; i < testDataLength; i++) {
            // recursively resolve value for collection members
            Object o = getValueForType(collectionMemberType, null, recursionLevel + 1);
            // and add it to the collection instance
            Method addMethod = type.getDeclaredMethod("add", Object.class);
            addMethod.invoke(collection, o);
        }
        return collection;
    }

    protected Object handleArrays(Class<?> type, GenerateForTest annotation, int recursionLevel)
            throws ReflectiveOperationException {

        int testDataLength = annotation.length();
        Class<?> arrayMemberType = type.getComponentType();
        Object array = Array.newInstance(arrayMemberType, testDataLength);
        for (int i = 0; i < testDataLength; i++) {
            Object o = getValueForType(arrayMemberType, null, recursionLevel + 1);
            Array.set(array, i, o);
        }
        return array;
    }

    public Object getValueForType(Class<?> type, GenerateForTest annotation) throws ReflectiveOperationException {
        return getValueForType(type, annotation, 0);
    }

    protected Object getValueForType(Class<?> type, GenerateForTest annotation, int recursionLevel)
            throws ReflectiveOperationException {

        // handle primitives and wrapper classes
        Object primitiveOrWrapper = handlePrimitivesAndWrappers(type);
        if (primitiveOrWrapper != null) {
            return primitiveOrWrapper;
        }

        // Handle collections
        if (Collection.class.isAssignableFrom(type)) {
            if (annotation != null) {
                return handleCollections(type, annotation, recursionLevel);
            } else {
                return null;
            }
        }

        // handle maps (unsupported)
        if (Map.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Maps are not supported.");
        }

        // handle enums
        if (type.isEnum()) {
            return getEnum(type);
        }

        // handle arrays
        if (type.isArray()) {
            if (annotation != null) {
                return handleArrays(type, annotation, recursionLevel);
            } else {
                return null;
            }
        }

        // after selecting-out "all" the other possibilities, we are probably handling a custom inner-bean
        // create the inner type
        if (recursionLevel == MAX_RECURSION_LEVEL) {
            log.fine("Maximum recursion level (" + recursionLevel + ") reached. Ignoring the field.");
            return null;
        }
        Object innerBean = type.newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            GenerateForTest subFieldAnnotation = field.getAnnotation(GenerateForTest.class);
            if (subFieldAnnotation != null) {
                field.setAccessible(true);
                // recursively gather the values for the containing fields and set it
                field.set(innerBean, getValueForType(field.getType(), subFieldAnnotation, recursionLevel + 1));
            }
        }
        return innerBean;
    }

}
