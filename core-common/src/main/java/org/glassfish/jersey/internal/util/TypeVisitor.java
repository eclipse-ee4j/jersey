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

package org.glassfish.jersey.internal.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Type visitor contract.
 *
 * @param <T> type visiting result type.
 * @author Kohsuke Kawaguchi
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
abstract class TypeVisitor<T> {

    /**
     * Visit the type and a given parameter.
     *
     * @param type visited type.
     * @return visiting result.
     */
    public final T visit(final Type type) {
        assert type != null;

        if (type instanceof Class) {
            return onClass((Class) type);
        }
        if (type instanceof ParameterizedType) {
            return onParameterizedType((ParameterizedType) type);
        }
        if (type instanceof GenericArrayType) {
            return onGenericArray((GenericArrayType) type);
        }
        if (type instanceof WildcardType) {
            return onWildcard((WildcardType) type);
        }
        if (type instanceof TypeVariable) {
            return onVariable((TypeVariable) type);
        }

        // covered all the cases
        assert false;

        throw createError(type);
    }

    /**
     * Visit class.
     *
     * @param clazz visited class.
     * @return visit result.
     */
    protected abstract T onClass(Class clazz);

    /**
     * Visit parameterized type.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected abstract T onParameterizedType(ParameterizedType type);

    /**
     * Visit generic array type.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected abstract T onGenericArray(GenericArrayType type);

    /**
     * Visit type variable.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected abstract T onVariable(TypeVariable type);

    /**
     * Visit wildcard type.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected abstract T onWildcard(WildcardType type);

    /**
     * Create visiting error (in case the visitor could not recognize the visit type.
     *
     * @param type visited parameterized type.
     * @return visit result.
     */
    protected RuntimeException createError(final Type type) {
        throw new IllegalArgumentException();
    }
}
