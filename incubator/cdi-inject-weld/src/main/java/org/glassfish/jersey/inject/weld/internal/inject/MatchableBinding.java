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

package org.glassfish.jersey.inject.weld.internal.inject;

import org.glassfish.jersey.internal.inject.Binding;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * A Binding to be able to be compared and matched in the runtime to be properly initialized.
 *
 * @param <T> Type of the bean described by this injection binding.
 * @param <D> Concrete injection binding implementation type.
 */
public abstract class MatchableBinding<T, D extends MatchableBinding> extends Binding<T, D> {

    protected abstract MatchLevel bestMatchLevel();

    protected Matching<D> matches(Binding<?, ?> other) {
        final Matching<D> matching = matchesContracts(other);
        if (matching.matchLevel == MatchLevel.FULL_CONTRACT) {
            if (getImplementationType().equals(other.getImplementationType())) {
                matching.matchLevel = MatchLevel.IMPLEMENTATION;
            }
        }
        return matching;
    }

    /**
     * Return a Matching object that represents comparison between contracts of this binding and a given binding.
     * The result contains a reference to this binding.
     * @param other
     * @return
     */
    public Matching<D> matchesContracts(Binding<?, ?> other) {
        boolean atLeastOneMatch = false;
        boolean allMatch = true;
        final Set<Type> firstContracts = getContracts();
        final Set<Type> secondContracts = other.getContracts();
        final Set<Type> biggerContracts = firstContracts.size() < secondContracts.size() ? secondContracts : firstContracts;
        final Set<Type> smallerContracts = firstContracts.size() < secondContracts.size() ? firstContracts : secondContracts;

        for (Type thisType : biggerContracts) {
            boolean aMatch = false;
            for (Type otherType : smallerContracts) {
                if (thisType.equals(otherType)) {
                    aMatch = true;
                    atLeastOneMatch = true;
                    break;
                }
            }
            if (!aMatch) {
                allMatch = false;
            }
        }
        final MatchLevel matchLevel = atLeastOneMatch
                ? (allMatch ? MatchLevel.FULL_CONTRACT : MatchLevel.PARTIAL_CONTRACT)
                : MatchLevel.NONE;
        final Matching<D> matching = new Matching<>((D) this, matchLevel);
        return matching;
    }

    /**
     * Matching object that represents the level of a match between two bindings. Contains a reference to a MatchableBinding
     * whose instance was used to create the Matching object.
     * @param <D> Concrete injection binding implementation type.
     */
    public static class Matching<D extends MatchableBinding> implements Comparable<Matching> {
        private D binding;
        protected MatchLevel matchLevel;

        public static <D extends MatchableBinding> Matching<D> noneMatching() {
            return new Matching<D>(null, MatchLevel.NONE);
        }

        protected Matching(D binding, MatchLevel matchLevel) {
            this.binding = binding;
            this.matchLevel = matchLevel;
        }

        @Override
        public int compareTo(Matching other) {
            return other.matchLevel.level - this.matchLevel.level;
        }

        public Matching better(Matching other) {
            return compareTo(other) <= 0 ? this : other;
        }

        public boolean isBest() {
            return matches() && matchLevel == binding.bestMatchLevel();
        }

        public boolean matches() {
            return matchLevel.level > MatchLevel.NONE.level;
        }

        public D getBinding() {
            return binding;
        }
    }

    /**
     * Internal granularity of a Matching.
     */
    protected static enum MatchLevel {
        NONE(0),
        PARTIAL_CONTRACT(1),
        FULL_CONTRACT(2),
        IMPLEMENTATION(3),
        SUPPLIER(4);

        private final int level;

        MatchLevel(int level) {
            this.level = level;
        }
    }
}

