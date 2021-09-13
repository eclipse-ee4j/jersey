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

package org.glassfish.jersey.inject.weld.internal.injector;

import jakarta.enterprise.context.spi.CreationalContext;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.producer.AbstractInstantiator;
import org.jboss.weld.manager.BeanManagerImpl;

import java.lang.reflect.Constructor;

/**
 * An implementation of an instantiator capable of instantiating different instance for the client and server side.
 * @param <T> the class of the instantiator.
 */
public class JerseyTwofoldInstantiator<T> extends AbstractInstantiator<T> {

    private final AbstractInstantiator<T> primaryInstantiator;
    private ConstructorInjectionPoint<T> optionalConstructorInjectionPoint = null;

    JerseyTwofoldInstantiator(AbstractInstantiator<T> serverInstantiator) {
        this.primaryInstantiator = serverInstantiator;
    }

    @Override
    public ConstructorInjectionPoint<T> getConstructorInjectionPoint() {
        return primaryInstantiator.getConstructorInjectionPoint();
    }

    @Override
    public boolean hasInterceptorSupport() {
        return primaryInstantiator.hasDecoratorSupport();
    }

    @Override
    public boolean hasDecoratorSupport() {
        return primaryInstantiator.hasDecoratorSupport();
    }

    @Override
    public Constructor<T> getConstructor() {
        return primaryInstantiator.getConstructor();
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        final ConstructorInjectionPoint<T> cip =
                optionalConstructorInjectionPoint == null || !JerseyClientCreationalContext.class.isInstance(ctx)
                ? primaryInstantiator.getConstructorInjectionPoint()
                : optionalConstructorInjectionPoint;
        return cip.newInstance(manager, ctx);
    }

    /**
     * Set the optional constuctor injection point for the client side instantiation.
     * @param optionalConstructorInjectionPoint The optional constructor injection point.
     */
    public void setOptionalConstructorInjectionPoint(ConstructorInjectionPoint<T> optionalConstructorInjectionPoint) {
        this.optionalConstructorInjectionPoint = optionalConstructorInjectionPoint;
    }
}
