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

package org.glassfish.jersey.internal.util.collection;

/**
 * A collection of {@link Ref reference} factory & utility methods.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class Refs {

    private Refs() {
        // preventing instantiation
    }

    /**
     * Immutable {@link Ref} implementation.
     */
    private static final class ImmutableRefImpl<T> implements Ref<T> {

        private final T reference;

        ImmutableRefImpl(final T value) {
            this.reference = value;
        }

        @Override
        public T get() {
            return reference;
        }

        @Override
        public void set(final T value) throws IllegalStateException {
            throw new IllegalStateException("This implementation of Ref interface is immutable.");
        }

        @Override
        public String toString() {
            return "ImmutableRefImpl{"
                    + "reference=" + reference
                    + '}';
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Ref)) {
                return false;
            }

            Object otherRef = ((Ref) obj).get();
            return this.reference == otherRef || (this.reference != null && this.reference.equals(otherRef));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + (this.reference != null ? this.reference.hashCode() : 0);
            return hash;
        }
    }

    /**
     * Default (mutable) {@link Ref} implementation. This implementation is not thread-safe.
     */
    private static final class DefaultRefImpl<T> implements Ref<T> {

        private T reference;

        DefaultRefImpl() {
            this.reference = null;
        }

        DefaultRefImpl(final T value) {
            this.reference = value;
        }

        @Override
        public T get() {
            return reference;
        }

        @Override
        public void set(final T value) throws IllegalStateException {
            this.reference = value;
        }

        @Override
        public String toString() {
            return "DefaultRefImpl{"
                   + "reference=" + reference
                   + '}';
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Ref)) {
                return false;
            }

            Object otherRef = ((Ref) obj).get();
            T ref = this.reference;
            return ref == otherRef || (ref != null && ref.equals(otherRef));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + (this.reference != null ? this.reference.hashCode() : 0);
            return hash;
        }
    }

    /**
     * Thread-safe {@link Ref} implementation.
     */
    private static final class ThreadSafeRefImpl<T> implements Ref<T> {

        private volatile T reference;

        ThreadSafeRefImpl() {
            this.reference = null;
        }

        ThreadSafeRefImpl(final T value) {
            this.reference = value;
        }

        @Override
        public T get() {
            return reference;
        }

        @Override
        public void set(final T value) throws IllegalStateException {
            this.reference = value;
        }

        @Override
        public String toString() {
            return "ThreadSafeRefImpl{"
                   + "reference=" + reference
                   + '}';
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Ref)) {
                return false;
            }

            Object otherRef = ((Ref) obj).get();
            T localRef = this.reference;
            return localRef == otherRef || (localRef != null && localRef.equals(otherRef));
        }

        @Override
        public int hashCode() {
            T localRef = this.reference;
            int hash = 5;
            hash = 47 * hash + (localRef != null ? localRef.hashCode() : 0);
            return hash;
        }
    }

    /**
     * Construct a new mutable {@link Ref} instance referencing the given
     * input value.
     *
     * @param <T>   type of the referenced instance.
     * @param value value of the newly constructed reference.
     * @return a new mutable {@link Ref} instance referencing the given
     *         input value.
     */
    public static <T> Ref<T> of(final T value) {
        return new DefaultRefImpl<T>(value);
    }

    /**
     * Construct a new empty mutable {@link Ref} instance.
     *
     * @param <T> type of the referenced instance.
     * @return a new mutable empty {@link Ref} instance.
     */
    public static <T> Ref<T> emptyRef() {
        return new DefaultRefImpl<T>();
    }

    /**
     * Construct a new empty mutable thread-safe {@link Ref} instance.
     *
     * @param <T> type of the referenced instance.
     * @return a new mutable empty thread-safe {@link Ref} instance.
     * @since 2.8
     */
    public static <T> Ref<T> threadSafe() {
        return new ThreadSafeRefImpl<T>();
    }

    /**
     * Construct a new mutable thread-safe {@link Ref} instance referencing the given
     * input value.
     *
     * @param <T> type of the referenced instance.
     * @param value value of the newly constructed reference.
     * @return a new mutable thread-safe {@link Ref} instance  referencing the given
     *         input value.
     * @since 2.8
     */
    public static <T> Ref<T> threadSafe(final T value) {
        return new ThreadSafeRefImpl<T>(value);
    }

    /**
     * Construct a new immutable {@link Ref} instance referencing the given
     * input value.
     * <p/>
     * Invoking a {@link Ref#set(java.lang.Object)} on the returned instance
     * will result in a {@link IllegalStateException} being thrown.
     *
     * @param <T>   type of the referenced instance.
     * @param value value of the newly constructed reference.
     * @return a new immutable {@link Ref} instance referencing the given
     *         input value.
     */
    public static <T> Ref<T> immutableRef(final T value) {
        return new ImmutableRefImpl<T>(value);
    }
}
