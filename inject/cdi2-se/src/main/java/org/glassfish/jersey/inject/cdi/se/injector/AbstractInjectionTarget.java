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

package org.glassfish.jersey.inject.cdi.se.injector;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * Abstract class which implements all methods from {@link InjectionTarget} by invoking the same methods on the delegate object.
 * Useful super class to extend and override only the needed method.
 *
 * @param <T> type of the injection target.
 * @author Petr Bouda
 */
abstract class AbstractInjectionTarget<T> implements InjectionTarget<T> {

    /**
     * Object on which all calls will be delegated.
     *
     * @return injection target.
     */
    abstract InjectionTarget<T> delegate();

    @Override
    public void inject(final T instance, final CreationalContext<T> ctx) {
        delegate().inject(instance, ctx);
    }

    @Override
    public void postConstruct(final T instance) {
        delegate().postConstruct(instance);
    }

    @Override
    public void preDestroy(final T instance) {
        delegate().preDestroy(instance);
    }

    @Override
    public T produce(final CreationalContext<T> ctx) {
        return delegate().produce(ctx);
    }

    @Override
    public void dispose(final T instance) {
        delegate().dispose(instance);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return delegate().getInjectionPoints();
    }
}
