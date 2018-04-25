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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.glassfish.jersey.internal.util.Pretty;

/**
 * An Injectee represents the point of injection. It can be used by injection resolvers to discover all of the information
 * available about the entity being injected into.
 */
public class InjecteeImpl implements Injectee {

    private Type requiredType;
    private Set<Annotation> qualifiers;
    private Class<? extends Annotation> parentClassScope;
    private int position = -1;
    private Class<?> injecteeClass;
    private AnnotatedElement parent;
    private boolean isOptional = false;
    private boolean isFactory = false;
    private boolean isProvider = false;
    private ForeignDescriptor injecteeDescriptor;

    @Override
    public Type getRequiredType() {
        return requiredType;
    }

    /**
     * Sets the required type of this Injectee.
     *
     * @param requiredType The required type of this injectee.
     */
    public void setRequiredType(Type requiredType) {
        this.requiredType = requiredType;
    }

    @Override
    public Set<Annotation> getRequiredQualifiers() {
        if (qualifiers == null) {
            return Collections.emptySet();
        }
        return qualifiers;
    }

    /**
     * Sets the required qualifiers for this Injectee.
     *
     * @param requiredQualifiers The non-null set of required qualifiers.
     */
    public void setRequiredQualifiers(Set<Annotation> requiredQualifiers) {
        qualifiers = Collections.unmodifiableSet(requiredQualifiers);

    }

    @Override
    public Class<? extends Annotation> getParentClassScope() {
        return parentClassScope;
    }


    /**
     * Sets the scope in which the parent class is registered.
     *
     * @return parent class scope.
     */
    public void setParentClassScope(Class<? extends Annotation> parentClassScope) {
        this.parentClassScope = parentClassScope;
    }

    @Override
    public boolean isFactory() {
        return isFactory;
    }

    /**
     * Sets a flag whether the injectee is a factory.
     *
     * @param factory {@code true} flag whether the injectee is factory.
     */
    public void setFactory(boolean factory) {
        isFactory = factory;
    }

    @Override
    public boolean isProvider() {
        return isProvider;
    }

    /**
     * Sets a flag whether the injectee is a provider.
     *
     * @param provider {@code true} flag whether the injectee is provider.
     */
    public void setProvider(boolean provider) {
        isProvider = provider;
    }

    @Override
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position of this Injectee. The position represents the index of the parameter, or {@code -1} if this Injectee is
     * describing a field.
     *
     * @param position The index position of the parameter, or {@code -1} if describing a field.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public Class<?> getInjecteeClass() {
        return injecteeClass;
    }

    /**
     * Sets type of the injectee.
     *
     * @param injecteeClass injectee type.
     */
    public void setInjecteeClass(final Class<?> injecteeClass) {
        this.injecteeClass = injecteeClass;
    }

    @Override
    public AnnotatedElement getParent() {
        return parent;
    }

    /**
     * This setter sets both the parent and the injecteeClass fields.
     *
     * @param parent The parent (Field, Constructor or Method) which is the parent of this Injectee.
     */
    public void setParent(AnnotatedElement parent) {
        this.parent = parent;

        if (parent instanceof Field) {
            injecteeClass = ((Field) parent).getDeclaringClass();
        } else if (parent instanceof Constructor) {
            injecteeClass = ((Constructor<?>) parent).getDeclaringClass();
        } else if (parent instanceof Method) {
            injecteeClass = ((Method) parent).getDeclaringClass();
        }
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }

    /**
     * Sets whether or not this Injectee should be considered optional.
     *
     * @param optional true if this injectee is optional, false if required.
     */
    public void setOptional(boolean optional) {
        this.isOptional = optional;
    }

    @Override
    public ForeignDescriptor getInjecteeDescriptor() {
        return injecteeDescriptor;
    }

    /**
     * Sets the descriptor for this Injectee.
     *
     * @param injecteeDescriptor injectee's descriptor.
     */
    public void setInjecteeDescriptor(ForeignDescriptor injecteeDescriptor) {
        this.injecteeDescriptor = injecteeDescriptor;
    }

    @Override
    public String toString() {
        return "InjecteeImpl(requiredType=" + Pretty.type(requiredType)
                + ",parent=" + Pretty.clazz(parent.getClass())
                + ",qualifiers=" + Pretty.collection(qualifiers)
                + ",position=" + position
                + ",factory=" + isFactory
                + ",provider=" + isProvider
                + ",optional=" + isOptional
                + "," + System.identityHashCode(this) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InjecteeImpl)) {
            return false;
        }
        InjecteeImpl injectee = (InjecteeImpl) o;
        return position == injectee.position
                && isOptional == injectee.isOptional
                && isFactory == injectee.isFactory
                && isProvider == injectee.isProvider
                && Objects.equals(requiredType, injectee.requiredType)
                && Objects.equals(qualifiers, injectee.qualifiers)
                && Objects.equals(injecteeClass, injectee.injecteeClass)
                && Objects.equals(parent, injectee.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requiredType, qualifiers, position, injecteeClass, parent, isOptional, isFactory);
    }
}
