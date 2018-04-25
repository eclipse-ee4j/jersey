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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * An Injectee represents the point of injection. It can be used by injection resolvers to discover all of the information
 * available about the entity being injected into.
 */
public interface Injectee {

    /**
     * This is the required type of the injectee. The object that is injected into this point must be type-safe with regards to
     * this type.
     *
     * @return The type that this injectee is expecting. Any object injected into this injection point must be type-safe with
     * regards to this type.
     */
    Type getRequiredType();

    /**
     * This is the set of required qualifiers for this injectee. All of these qualifiers must be present on the implementation
     * class of the object that is injected into this injectee. Note that the fields of the annotation must also match.
     *
     * @return Will not return {@code null}, but may return an empty set. The set of all qualifiers that must match.
     */
    Set<Annotation> getRequiredQualifiers();

    /**
     * If this Injectee is a constructor or method parameter, this will return the index of the parameter. If this
     * {@code Injectee} is a field, this will return {@code -1}.
     *
     * @return the position of the parameter, or {@code -1} if this is a field.
     */
    int getPosition();

    /**
     * Returns the parent class for this injectee. This is the class of the object that will be injected into. This field may
     * return {@code null} if this is from a service lookup.
     *
     * @return The class of the object that will be injected into.
     */
    Class<?> getInjecteeClass();

    /**
     * If this Injectee is in a constructor this will return the constructor being injected into. If this Injectee is in a
     * method this will return the method being injected into. If this injectee represents a field, this will return the field
     * being injected into. This injectee may be neither in which case this will return {@code null}.
     *
     * @return The parent of the injectee, which may be {@code null}.
     */
    AnnotatedElement getParent();

    /**
     * This method returns {@code true} if this injection point is annotated with &#86;Optional. In this case if there is no
     * definition for  the injection point in the system it is allowable for the system to merely return {@code null}.
     *
     * @return {@code true} if the injection point is annotated with &#86;Optional, {@code false} otherwise.
     */
    boolean isOptional();

    /**
     * This method returns foreign descriptor of the current injectee that means that the DI provider is able to store its
     * specific descriptor and that use it in the descriptor processing.
     *
     * @return DI specific foreign descriptor.
     */
    ForeignDescriptor getInjecteeDescriptor();

    /**
     * This method returns scope in which the parent class is registered.
     *
     * @return scope annotation.
     */
    Class<? extends Annotation> getParentClassScope();

    /**
     * This method returns {@code true} if the injectee value is provided using {@link java.util.function.Supplier}.
     *
     * @return {@code true} if the injectee is a factory.
     */
    boolean isFactory();

    /**
     * This method returns {@code true} if the injectee value is provided using {@link javax.inject.Provider}.
     *
     * @return {@code true} if the injectee is a provider.
     */
    boolean isProvider();
}
