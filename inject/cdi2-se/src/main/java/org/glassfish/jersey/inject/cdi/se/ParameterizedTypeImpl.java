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

package org.glassfish.jersey.inject.cdi.se;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Simple implementation of {@link ParameterizedType}.
 * <p>
 * John Wells (john.wells at oracle.com)
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private final Type rawType;
    private final Type actualTypeArguments[];

    /**
     * A new parameterized type.
     *
     * @param rawType             The raw type of this type.
     * @param actualTypeArguments The actual type arguments.
     */
    public ParameterizedTypeImpl(Type rawType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.ParameterizedType#getActualTypeArguments()
     */
    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.ParameterizedType#getRawType()
     */
    @Override
    public Type getRawType() {
        return rawType;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.ParameterizedType#getOwnerType()
     * This is only used for top level types
     */
    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public int hashCode() {
        int retVal = Arrays.hashCode(actualTypeArguments);
        if (rawType == null) {
            return retVal;
        }
        return retVal ^ rawType.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ParameterizedType)) {
            return false;
        }

        ParameterizedType other = (ParameterizedType) o;

        if (!rawType.equals(other.getRawType())) {
            return false;
        }

        Type otherActuals[] = other.getActualTypeArguments();

        if (otherActuals.length != actualTypeArguments.length) {
            return false;
        }

        for (int lcv = 0; lcv < otherActuals.length; lcv++) {
            if (!actualTypeArguments[lcv].equals(otherActuals[lcv])) {
                return false;
            }
        }

        return true;
    }
}
