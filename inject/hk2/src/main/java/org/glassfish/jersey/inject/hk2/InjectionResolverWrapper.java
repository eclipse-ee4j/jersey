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

import java.lang.annotation.Annotation;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.ForeignDescriptorImpl;
import org.glassfish.jersey.internal.inject.InjecteeImpl;
import org.glassfish.jersey.internal.util.ReflectionHelper;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;

/**
 * This class wraps the jersey class {@link org.glassfish.jersey.internal.inject.InjectionResolver} to make HK2 version of this
 * provided functionality. HK2 {@link InjectionResolver} can be then register in {@link org.glassfish.hk2.api.ServiceLocator} and
 * HK2 can handle the annotation which is register along with the interface.
 */
@Singleton
public class InjectionResolverWrapper<T extends Annotation> implements InjectionResolver<T> {

    private final org.glassfish.jersey.internal.inject.InjectionResolver jerseyResolver;

    /**
     * C'tor accepts jersey-like {@code InjectionResolver} on which the the processing is delegated.
     *
     * @param jerseyResolver jersey injection resolver.
     */
    InjectionResolverWrapper(org.glassfish.jersey.internal.inject.InjectionResolver<T> jerseyResolver) {
        this.jerseyResolver = jerseyResolver;
    }

    @Override
    public Object resolve(Injectee injectee, ServiceHandle root) {
        InjecteeImpl injecteeWrapper = new InjecteeImpl();
        injecteeWrapper.setRequiredType(injectee.getRequiredType());
        injecteeWrapper.setParent(injectee.getParent());
        injecteeWrapper.setRequiredQualifiers(injectee.getRequiredQualifiers());
        injecteeWrapper.setOptional(injectee.isOptional());
        injecteeWrapper.setPosition(injectee.getPosition());
        injecteeWrapper.setFactory(ReflectionHelper.isSubClassOf(injectee.getRequiredType(), Factory.class));
        injecteeWrapper.setInjecteeDescriptor(new ForeignDescriptorImpl(injectee.getInjecteeDescriptor()));

        Object instance = jerseyResolver.resolve(injecteeWrapper);
        if (injecteeWrapper.isFactory()) {
            return asFactory(instance);
        } else {
            return instance;
        }
    }

    private Factory asFactory(Object instance) {
        return new Factory() {
            @Override
            public Object provide() {
                return instance;
            }

            @Override
            public void dispose(final Object instance) {
            }
        };
    }

    @Override
    public boolean isConstructorParameterIndicator() {
        return jerseyResolver.isConstructorParameterIndicator();
    }

    @Override
    public boolean isMethodParameterIndicator() {
        return jerseyResolver.isMethodParameterIndicator();
    }
}
