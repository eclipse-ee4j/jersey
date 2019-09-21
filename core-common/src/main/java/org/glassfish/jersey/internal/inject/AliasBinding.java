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

package org.glassfish.jersey.internal.inject;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Injection binding description used to describe the aliases to main {@link Binding}.
 *
 * @author Petr Bouda
 */
public class AliasBinding {

    private final Class<?> contract;
    private final Set<Annotation> qualifiers = new LinkedHashSet<>();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> scope = Optional.empty();
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private OptionalInt rank = OptionalInt.empty();

    /**
     * Creates a new alias.
     *
     * @param contract contract of the alias.
     */
     /* package */ AliasBinding(Class<?> contract) {
        this.contract = contract;
    }

    /**
     * Gets binding's contract.
     *
     * @return binding's contract.
     */
    public Class<?> getContract() {
        return contract;
    }

    /**
     * Gets binding's optional scope.
     *
     * @return binding's scope, if set explicitly.
     */
    public Optional<String> getScope() {
        return scope;
    }

    /**
     * Sets the binding's scope.
     *
     * @param scope binding's scope.
     * @return current instance.
     */
    public AliasBinding in(String scope) {
        this.scope = Optional.of(scope);

        return this;
    }

    /**
     * Gets binding's optional rank.
     *
     * @return binding's rank, if set explicitly.
     */
    public OptionalInt getRank() {
        return rank;
    }

    /**
     * Sets the binding's rank.
     *
     * @param rank binding's rank.
     * @return current instance.
     */
    public AliasBinding ranked(int rank) {
        this.rank = OptionalInt.of(rank);

        return this;
    }

    /**
     * Gets binding's qualifiers.
     *
     * @return binding's qualifiers
     */
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    /**
     * Adds a new binding's qualifier.
     *
     * @param annotation binding's qualifier.
     * @return current instance.
     */
    public AliasBinding qualifiedBy(Annotation annotation) {
        if (annotation != null) {
            qualifiers.add(annotation);
        }

        return this;
    }
}
