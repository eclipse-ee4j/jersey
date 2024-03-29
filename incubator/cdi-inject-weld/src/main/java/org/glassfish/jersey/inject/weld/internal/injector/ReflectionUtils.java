/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.weld.internal.injector;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.jersey.inject.weld.internal.type.GenericArrayTypeImpl;
import org.glassfish.jersey.inject.weld.internal.type.ParameterizedTypeImpl;
import org.glassfish.jersey.internal.util.collection.ImmutableCollectors;

/**
 * Utility class for getting the information from class using Reflection API.
 *
 * @author John Wells (john.wells at oracle.com)
 * @author Petr Bouda
 */
class ReflectionUtils {

    private static final Logger LOGGER = Logger.getLogger(ReflectionUtils.class.getName());

    static Set<Field> getAllFields(Class<?> clazz) {
        if (clazz == null || Object.class.equals(clazz)) {
            return Collections.emptySet();
        }
        if (clazz.isInterface()) {
            return Collections.emptySet();
        }

        Set<Field> retVal = new LinkedHashSet<>();
        retVal.addAll(getDeclaredField(clazz));
        retVal.addAll(getAllFields(clazz.getSuperclass()));
        return retVal;
    }

    /**
     * Gets the EXACT set of fields on this class only. No subclasses. So this set should be considered RAW and has not taken into
     * account any subclasses.
     *
     * @param clazz The class to examine.
     * @return all declared fields.
     */
    private static Set<Field> getDeclaredField(final Class<?> clazz) {
        return Arrays.stream(secureGetDeclaredFields(clazz)).collect(ImmutableCollectors.toImmutableLinkedSet());
    }

    private static Field[] secureGetDeclaredFields(final Class<?> clazz) {
        return AccessController.doPrivileged((PrivilegedAction<Field[]>) clazz::getDeclaredFields);
    }

    /**
     * Resolves the generic type of a field given the actual class being instantiated
     *
     * @param topclass The instantiation class.  Must not be null
     * @param field    The non-null field whose type to resolve
     * @return The resolved field type by way of its subclasses.  Will not return
     * null, but may return the original fields generic type
     */
    static Type resolveField(Class<?> topclass, Field field) {
        return resolveMember(topclass, field.getGenericType(), field.getDeclaringClass());
    }

    /**
     * Resolves the generic type of a type and declaring class given the actual class being instantiated
     *
     * @param topclass       The instantiation class.  Must not be null
     * @param lookingForType The type to resolve.  Must not be null
     * @param declaringClass The class of the entity declaring the lookingForType. Must not be null
     * @return The resolved type by way of its subclasses.  Will not return null but may
     * return lookingForType if it could not be further resolved
     */
    private static Type resolveMember(Class<?> topclass, Type lookingForType, Class<?> declaringClass) {
        Map<String, Type> typeArguments = typesFromSubClassToDeclaringClass(topclass, declaringClass);
        if (typeArguments == null) {
            return lookingForType;
        }

        if (lookingForType instanceof ParameterizedType) {
            return fixTypeVariables((ParameterizedType) lookingForType, typeArguments);
        }

        if (lookingForType instanceof GenericArrayType) {
            return fixGenericArrayTypeVariables((GenericArrayType) lookingForType, typeArguments);
        }

        if (!(lookingForType instanceof TypeVariable)) {
            return lookingForType;
        }

        TypeVariable<?> tv = (TypeVariable<?>) lookingForType;
        String typeVariableName = tv.getName();

        Type retVal = typeArguments.get(typeVariableName);
        if (retVal == null) {
            return lookingForType;
        }

        if (retVal instanceof Class) {
            return retVal;
        }

        if (retVal instanceof ParameterizedType) {
            return fixTypeVariables((ParameterizedType) retVal, typeArguments);
        }

        if (retVal instanceof GenericArrayType) {
            return fixGenericArrayTypeVariables((GenericArrayType) retVal, typeArguments);
        }

        return retVal;
    }

    private static Map<String, Type> typesFromSubClassToDeclaringClass(Class<?> topClass, Class<?> declaringClass) {
        if (topClass.equals(declaringClass)) {
            return null;
        }

        Type superType = topClass.getGenericSuperclass();
        Class<?> superClass = getRawClass(superType);

        while (superType != null && superClass != null) {
            if (!(superType instanceof ParameterizedType)) {
                // superType MUST be a Class in this case
                if (superClass.equals(declaringClass)) {
                    return null;
                }

                superType = superClass.getGenericSuperclass();
                superClass = getRawClass(superType);

                continue;
            }

            ParameterizedType superPT = (ParameterizedType) superType;

            Map<String, Type> typeArguments = getTypeArguments(superClass, superPT);

            if (superClass.equals(declaringClass)) {
                return typeArguments;
            }

            superType = superClass.getGenericSuperclass();
            superClass = getRawClass(superType);

            if (superType instanceof ParameterizedType) {
                superType = fixTypeVariables((ParameterizedType) superType, typeArguments);
            }
        }

        throw new AssertionError(topClass.getName() + " is not the same as or a subclass of " + declaringClass.getName());
    }

    /**
     * Gets a mapping of type variable names of the raw class to type arguments of the parameterized type.
     */
    private static Map<String, Type> getTypeArguments(Class<?> rawClass, ParameterizedType type) {
        Map<String, Type> typeMap = new HashMap<>();
        Type[] typeArguments = type.getActualTypeArguments();

        int i = 0;
        for (TypeVariable<?> typeVariable : rawClass.getTypeParameters()) {
            typeMap.put(typeVariable.getName(), typeArguments[i++]);
        }
        return typeMap;
    }

    /**
     * Replace any TypeVariables in the given type's arguments with
     * the actual argument types.  Return the given type if no replacing
     * is required.
     */
    private static Type fixTypeVariables(ParameterizedType type, Map<String, Type> typeArgumentsMap) {
        Type[] newTypeArguments = getNewTypeArguments(type, typeArgumentsMap);
        if (newTypeArguments != null) {
            return new ParameterizedTypeImpl(type.getRawType(), newTypeArguments);
        }
        return type;
    }

    /**
     * Get a new array of type arguments for the given ParameterizedType, replacing any TypeVariables with
     * actual types.  The types should be found in the given arguments map, keyed by variable name.  Return
     * null if no arguments needed to be replaced.
     */
    private static Type[] getNewTypeArguments(final ParameterizedType type,
            final Map<String, Type> typeArgumentsMap) {

        Type[] typeArguments = type.getActualTypeArguments();
        Type[] newTypeArguments = new Type[typeArguments.length];
        boolean newArgsNeeded = false;

        int i = 0;
        for (Type argType : typeArguments) {
            if (argType instanceof TypeVariable) {
                newTypeArguments[i] = typeArgumentsMap.get(((TypeVariable<?>) argType).getName());
                newArgsNeeded = true;
            } else if (argType instanceof ParameterizedType) {
                ParameterizedType original = (ParameterizedType) argType;

                Type[] internalTypeArgs = getNewTypeArguments(original, typeArgumentsMap);
                if (internalTypeArgs != null) {
                    newTypeArguments[i] = new ParameterizedTypeImpl(original.getRawType(), internalTypeArgs);
                    newArgsNeeded = true;
                } else {
                    newTypeArguments[i] = argType;
                }
            } else if (argType instanceof GenericArrayType) {
                GenericArrayType gat = (GenericArrayType) argType;

                Type internalTypeArg = getNewTypeArrayArguments(gat, typeArgumentsMap);
                if (internalTypeArg != null) {
                    if (internalTypeArg instanceof Class<?>) {
                        newTypeArguments[i] = getArrayOfType((Class<?>) internalTypeArg);
                        newArgsNeeded = true;
                    } else if ((internalTypeArg instanceof ParameterizedType)
                            && (((ParameterizedType) internalTypeArg).getRawType() instanceof Class<?>)) {
                        ParameterizedType pt = (ParameterizedType) internalTypeArg;

                        newTypeArguments[i] = getArrayOfType((Class<?>) pt.getRawType());
                        newArgsNeeded = true;
                    } else {
                        newTypeArguments[i] = new GenericArrayTypeImpl(internalTypeArg);
                        newArgsNeeded = true;
                    }
                } else {
                    newTypeArguments[i] = argType;
                }
            } else {
                newTypeArguments[i] = argType;
            }

            i++;
        }

        return newArgsNeeded ? newTypeArguments : null;
    }

    /**
     * Get a new Type for a GenericArrayType, replacing any TypeVariables with
     * actual types.  The types should be found in the given arguments map, keyed by variable name.  Return
     * null if no arguments needed to be replaced.
     */
    private static Type getNewTypeArrayArguments(final GenericArrayType gat,
            final Map<String, Type> typeArgumentsMap) {

        Type typeArgument = gat.getGenericComponentType();

        if (typeArgument instanceof TypeVariable) {
            return typeArgumentsMap.get(((TypeVariable<?>) typeArgument).getName());
        }

        if (typeArgument instanceof ParameterizedType) {
            ParameterizedType original = (ParameterizedType) typeArgument;

            Type[] internalTypeArgs = getNewTypeArguments(original, typeArgumentsMap);
            if (internalTypeArgs != null) {
                return new ParameterizedTypeImpl(original.getRawType(), internalTypeArgs);
            }

            return original;
        }

        if (typeArgument instanceof GenericArrayType) {
            GenericArrayType original = (GenericArrayType) typeArgument;

            Type internalTypeArg = getNewTypeArrayArguments(original, typeArgumentsMap);
            if (internalTypeArg != null) {
                if (internalTypeArg instanceof Class<?>) {
                    return getArrayOfType((Class<?>) internalTypeArg);
                }

                if (internalTypeArg instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) internalTypeArg;

                    if (pt.getRawType() instanceof Class<?>) {
                        return getArrayOfType((Class<?>) pt.getRawType());
                    }
                }

                return new GenericArrayTypeImpl(internalTypeArg);
            }

            return null;
        }

        return null;
    }

    /**
     * Replace any TypeVariables in the given type's arguments with
     * the actual argument types.  Return the given type if no replacing
     * is required.
     */
    private static Type fixGenericArrayTypeVariables(GenericArrayType type, Map<String, Type> typeArgumentsMap) {
        Type newTypeArgument = getNewTypeArrayArguments(type, typeArgumentsMap);

        if (newTypeArgument != null) {
            if (newTypeArgument instanceof Class<?>) {
                return getArrayOfType((Class<?>) newTypeArgument);
            }

            if (newTypeArgument instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) newTypeArgument;
                if (pt.getRawType() instanceof Class<?>) {
                    return getArrayOfType((Class<?>) pt.getRawType());
                }
            }

            return new GenericArrayTypeImpl(newTypeArgument);
        }

        return type;
    }

    private static Class<?> getArrayOfType(Class<?> type) {
        return Array.newInstance(type, 0).getClass();
    }

    /**
     * Given the type parameter gets the raw type represented
     * by the type, or null if this has no associated raw class
     *
     * @param type The type to find the raw class on
     * @return The raw class associated with this type
     */
    private static Class<?> getRawClass(Type type) {
        if (type == null) {
            return null;
        }

        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();

            if (!(componentType instanceof ParameterizedType) && !(componentType instanceof Class)) {
                // type variable is not supported
                return null;
            }

            Class<?> rawComponentClass = getRawClass(componentType);

            String forNameName = "[L" + rawComponentClass.getName() + ";";
            try {
                return Class.forName(forNameName);
            } catch (Throwable th) {
                // ignore, but return null
                return null;
            }
        }

        if (type instanceof Class) {
            return (Class<?>) type;
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                return (Class<?>) rawType;
            }
        }

        return null;
    }

    /**
     * Sets the given field to the given value
     *
     * @param field    The non-null field to set
     * @param instance The non-null instance to set into
     * @param value    The value to which the field should be set
     * @throws Throwable If there was some exception while setting the field
     */
    static void setField(Field field, Object instance, Object value) throws Throwable {
        setAccessible(field);

        try {
            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.warning(String.format("Failed during setting a value into Class: %s, Field: %s",
                    field.getDeclaringClass().getName(), field.getName()));
            throw ex;
        }
    }

    /**
     * Sets this accessible object to be accessible using the permissions of
     * the hk2-locator bundle (which will need the required grant)
     *
     * @param ao The object to change
     */
    private static void setAccessible(final AccessibleObject ao) {
        if (ao.isAccessible()) {
            return;
        }

        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            ao.setAccessible(true);
            return null;
        });
    }
}
