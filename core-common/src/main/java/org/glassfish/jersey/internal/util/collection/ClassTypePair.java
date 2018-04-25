/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util.collection;

import java.lang.reflect.Type;

/**
 * A pair of raw class and the related type.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class ClassTypePair {

    private final Type type;
    private final Class<?> rawClass;

    private ClassTypePair(Class<?> c, Type t) {
        this.type = t;
        this.rawClass = c;
    }

    /**
     * Get the raw class of the {@link #type() type}.
     *
     * @return raw class of the type.
     */
    public Class<?> rawClass() {
        return rawClass;
    }

    /**
     * Get the actual type behind the {@link #rawClass() raw class}.
     *
     * @return the actual type behind the raw class.
     */
    public Type type() {
        return type;
    }

    /**
     * Create new type-class pair for a non-generic class.
     *
     * @param rawClass (raw) class representing the non-generic type.
     *
     * @return new non-generic type-class pair.
     */
    public static ClassTypePair of(Class<?> rawClass) {
        return new ClassTypePair(rawClass, rawClass);
    }

    /**
     * Create new type-class pair.
     *
     * @param rawClass raw class representing the type.
     * @param type type behind the class.
     *
     * @return new type-class pair.
     */
    public static ClassTypePair of(Class<?> rawClass, Type type) {
        return new ClassTypePair(rawClass, type);
    }
}
