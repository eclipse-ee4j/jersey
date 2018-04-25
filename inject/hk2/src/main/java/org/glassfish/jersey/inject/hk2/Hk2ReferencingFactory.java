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

package org.glassfish.jersey.inject.hk2;

import javax.inject.Provider;

import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.internal.util.collection.Refs;

import org.glassfish.hk2.api.Factory;

/**
 * Factory that provides injection of the referenced instance.
 *
 * @param <T>
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class Hk2ReferencingFactory<T> implements Factory<T> {

    private static class EmptyReferenceFactory<T> implements Factory<Ref<T>> {

        @Override
        public Ref<T> provide() {
            return Refs.emptyRef();
        }

        @Override
        public void dispose(Ref<T> instance) {
            //not used
        }
    }

    private static class InitializedReferenceFactory<T> implements Factory<Ref<T>> {

        private final T initialValue;

        public InitializedReferenceFactory(T initialValue) {
            this.initialValue = initialValue;
        }

        @Override
        public Ref<T> provide() {
            return Refs.of(initialValue);
        }

        @Override
        public void dispose(Ref<T> instance) {
            //not used
        }
    }

    private final Provider<Ref<T>> referenceFactory;

    /**
     * Create new referencing injection factory.
     *
     * @param referenceFactory reference provider backing the factory.
     */
    public Hk2ReferencingFactory(Provider<Ref<T>> referenceFactory) {
        this.referenceFactory = referenceFactory;
    }

    @Override
    public T provide() {
        return referenceFactory.get().get();
    }

    @Override
    public void dispose(T instance) {
        //not used
    }

    /**
     * Get a reference factory providing an empty reference.
     *
     * @param <T> reference type.
     * @return reference factory providing an empty reference.
     */
    public static <T> Factory<Ref<T>> referenceFactory() {
        return new EmptyReferenceFactory<T>();
    }

    /**
     * Get a reference factory providing an initialized reference.
     *
     * @param <T>          reference type.
     * @param initialValue initial value stored in the reference provided
     *                     by the returned factory.
     * @return reference factory providing a reference initialized with an
     *         {@code initialValue}.
     */
    public static <T> Factory<Ref<T>> referenceFactory(T initialValue) {
        if (initialValue == null) {
            return new EmptyReferenceFactory<T>();
        }

        return new InitializedReferenceFactory<T>(initialValue);
    }
}
