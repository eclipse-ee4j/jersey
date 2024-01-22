/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.Beta;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jakarta.ws.rs.core.GenericType;

import jakarta.inject.Named;

/**
 * Abstract injection binding description of a bean.
 *
 * @param <T> type of the bean described by this injection binding.
 * @param <D> concrete injection binding implementation type.
 * @author Petr Bouda
 */
@SuppressWarnings("unchecked")
public abstract class Binding<T, D extends Binding> {

    private final Set<Type> contracts = new HashSet<>();
    private final Set<Annotation> qualifiers = new HashSet<>();
    private final Set<AliasBinding> aliases = new HashSet<>();
    private Class<? extends Annotation> scope = null;
    private String name = null;
    private Class<T> implementationType = null;
    private String analyzer = null;
    private Boolean proxiable = null;
    private Boolean proxyForSameScope = null;
    private Integer ranked = null;
    private boolean forClient = false;
    private long id = 0;

    /**
     * Gets information whether the service is proxiable.
     *
     * @return {@code true} if the service is proxiable.
     */
    public Boolean isProxiable() {
        return proxiable;
    }

    /**
     * Gets information whether the service creates the proxy for the same scope.
     *
     * @return {@code true} if the service creates the proxy for the same scop.
     */
    public Boolean isProxiedForSameScope() {
        return proxyForSameScope;
    }

    /**
     * Gets rank of the service.
     *
     * @return service's rank.
     */
    public Integer getRank() {
        return ranked;
    }

    /**
     * Gets service's contracts.
     *
     * @return service's contracts.
     */
    public Set<Type> getContracts() {
        return contracts;
    }

    /**
     * Gets service's qualifiers.
     *
     * @return service's qualifiers.
     */
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    /**
     * Gets service's scope.
     *
     * @return service's scope.
     */
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    /**
     * Gets service's name.
     *
     * @return service's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets service's type.
     *
     * @return service's type.
     */
    public Class<T> getImplementationType() {
        return implementationType;
    }

    /**
     * Gets service's analyzer.
     *
     * @return service's analyzer.
     */
    public String getAnalyzer() {
        return analyzer;
    }

    /**
     * Gets service's aliases.
     *
     * @return service's aliases.
     */
    public Set<AliasBinding> getAliases() {
        return aliases;
    }

    /**
     * Get the hint of the bean that exists both on the client and the server
     * @return the hint that this binding if for being injected on the client.
     */
    @Beta
    public boolean isForClient() {
        return forClient;
    }

    /**
     * Adds service's analyzer.
     *
     * @return current instance.
     */
    // TODO: Candidate to remove, used only in legacy CDI integration.
    public D analyzeWith(String analyzer) {
        this.analyzer = analyzer;
        return (D) this;
    }

    /**
     * Adds service's contracts.
     *
     * @return current instance.
     */
    public D to(Collection<Class<? super T>> contracts) {
        if (contracts != null) {
            this.contracts.addAll(contracts);
        }
        return (D) this;
    }

    /**
     * Adds service's contract.
     *
     * @return current instance.
     */
    public D to(Class<? super T> contract) {
        this.contracts.add(contract);
        return (D) this;
    }

    /**
     * Adds service's contract.
     *
     * @return current instance.
     */
    public D to(GenericType<?> contract) {
        this.contracts.add(contract.getType());
        return (D) this;
    }

    /**
     * Adds service's contract.
     *
     * @return current instance.
     */
    public D to(Type contract) {
        this.contracts.add(contract);
        return (D) this;
    }

    /**
     * Adds service's qualifier.
     *
     * @return current instance.
     */
    public D qualifiedBy(Annotation annotation) {
        if (Named.class.equals(annotation.annotationType())) {
            this.name = ((Named) annotation).value();
        }
        this.qualifiers.add(annotation);
        return (D) this;
    }

    /**
     * Adds service's scope.
     *
     * @return current instance.
     */
    public D in(Class<? extends Annotation> scopeAnnotation) {
        this.scope = scopeAnnotation;
        return (D) this;
    }

    /**
     * Adds service's name.
     *
     * @return current instance.
     */
    public D named(String name) {
        this.name = name;
        return (D) this;
    }

    /**
     * Adds service's alias.
     *
     * @param contract contract of the alias.
     * @return instance of a new alias for this binding descriptor that can be further specified.
     */
    public AliasBinding addAlias(Class<?> contract) {
        AliasBinding alias = new AliasBinding(contract);
        aliases.add(alias);
        return alias;
    }

    /**
     * Adds information about proxy creation.
     *
     * @return current instance.
     */
    public D proxy(boolean proxiable) {
        this.proxiable = proxiable;
        return (D) this;
    }

    /**
     * Adds information about proxy creation when the service is in the same scope.
     *
     * @return current instance.
     */
    public D proxyForSameScope(boolean proxyForSameScope) {
        this.proxyForSameScope = proxyForSameScope;
        return (D) this;
    }

    /**
     * Adds service's rank.
     *
     * @return current instance.
     */
    public D ranked(int rank) {
        this.ranked = rank;
        return (D) this;
    }

    /**
     * Adds service's type.
     *
     * @return current instance.
     */
    D asType(Class type) {
        this.implementationType = type;
        return (D) this;
    }

    /**
     * A hint whether the bean is for being injected using {@code @Inject} on the client side
     * but there is also a bean with the same contract on the server, such as for {@code Configuration}.
     *
     * <p>Default is false.</p>
     *
     * @param forClient {@code true} when injectable for the same contract exists both on the client and server and
     *                              this binding is for the client side.
     * @return current instance.
     */
    @Beta
    public D forClient(boolean forClient) {
        this.forClient = forClient;
        return (D) this;
    }

    /**
     * The binding id used to match the binding in the pre-binding phase and in the binding phase.
     * @param id the unique id.
     * @return current instance.
     */
    @Beta
    public D id(long id) {
        // 1000 - 1999 Core-Common
        // 2000 - 2999 Core-Client
        // 3000 - 3999 Core-Server
        this.id = id;
        return (D) this;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return contractsAsString() + " <- " + implementationType.getSimpleName();
    }

    protected String contractsAsString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Type> it = contracts.iterator();
        while (it.hasNext()) {
            Type next = it.next();
            if (Class.class.isInstance(next)) {
                sb.append(((Class) next).getSimpleName());
            } else if (ParameterizedType.class.isInstance(next)) {
                sb.append(next);
            }
        }
        return sb.toString();
    }

}
