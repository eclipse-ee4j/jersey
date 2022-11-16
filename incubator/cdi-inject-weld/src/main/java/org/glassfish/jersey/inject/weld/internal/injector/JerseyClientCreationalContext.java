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

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.glassfish.jersey.inject.weld.internal.managed.CdiClientInjectionManager;
import org.jboss.weld.construction.api.AroundConstructCallback;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.contexts.CreationalContextImpl;
import org.jboss.weld.injection.spi.ResourceReference;
import org.jboss.weld.interceptor.proxy.InterceptionContext;

import java.util.List;

/**
 * Jersey implementation of CreationalContext holding an instance of the client InjectionManager.
 * Should be used on the client side only. Wraps the original context.
 * @param <T> the class of the creational context.
 */
public class JerseyClientCreationalContext<T> extends CreationalContextImpl<T> {

    private final CreationalContextImpl<T> wrapped;
    private CdiClientInjectionManager injectionManager = null;

    public JerseyClientCreationalContext(CreationalContextImpl<T> wrapped) {
        super(wrapped.getContextual());
        this.wrapped = wrapped;
    }

    @Override
    public <S> CreationalContextImpl<S> getCreationalContext(Contextual<S> contextual) {
        return new JerseyClientCreationalContext<>(wrapped.getCreationalContext(contextual))
                .setInjectionManager(injectionManager);
    }

    public <S> CreationalContextImpl<S> getProducerReceiverCreationalContext(Contextual<S> contextual) {
        return new JerseyClientCreationalContext<>(wrapped.getProducerReceiverCreationalContext(contextual))
                .setInjectionManager(injectionManager);
    }

    public <S> S getIncompleteInstance(Contextual<S> bean) {
        return wrapped.getIncompleteInstance(bean);
    }

    public boolean containsIncompleteInstance(Contextual<?> bean) {
        return wrapped.containsIncompleteInstance(bean);
    }

    public void addDependentInstance(ContextualInstance<?> contextualInstance) {
        wrapped.addDependentInstance(contextualInstance);
    }

    public void release() {
        wrapped.release();
    }

    public void release(Contextual<T> contextual, T instance) {
        wrapped.release(contextual, instance);
    }

    /**
     * @return the parent {@link CreationalContext} or null if there isn't any parent.
     */
    public CreationalContextImpl<?> getParentCreationalContext() {
        return wrapped.getParentCreationalContext();
    }

    /**
     * Returns an unmodifiable list of dependent instances.
     */
    public List<ContextualInstance<?>> getDependentInstances() {
        return wrapped.getDependentInstances();
    }

    /**
     * Register a {@link ResourceReference} as a dependency. {@link ResourceReference#release()} will be called on every {@link ResourceReference} once this
     * {@link CreationalContext} instance is released.
     */
    public void addDependentResourceReference(ResourceReference<?> resourceReference) {
        wrapped.addDependentResourceReference(resourceReference);
    }

    /**
     * Destroys dependent instance
     *
     * @param instance
     * @return true if the instance was destroyed, false otherwise
     */
    public boolean destroyDependentInstance(T instance) {
        return wrapped.destroyDependentInstance(instance);
    }

    /**
     * @return the {@link Contextual} for which this {@link CreationalContext} is created.
     */
    public Contextual<T> getContextual() {
        return wrapped.getContextual();
    }

    public List<AroundConstructCallback<T>> getAroundConstructCallbacks() {
        return wrapped.getAroundConstructCallbacks();
    }

    @Override
    public void setConstructorInterceptionSuppressed(boolean value) {
        wrapped.setConstructorInterceptionSuppressed(value);
    }

    @Override
    public boolean isConstructorInterceptionSuppressed() {
        return wrapped.isConstructorInterceptionSuppressed();
    }

    @Override
    public void registerAroundConstructCallback(AroundConstructCallback<T> callback) {
        wrapped.registerAroundConstructCallback(callback);
    }

    /**
     *
     * @return the interception context used for Weld-managed AroundConstruct interceptors or <code>null</code> if no such interceptors were applied
     */
    public InterceptionContext getAroundConstructInterceptionContext() {
        return wrapped.getAroundConstructInterceptionContext();
    }

    public void setAroundConstructInterceptionContext(InterceptionContext aroundConstructInterceptionContext) {
        wrapped.setAroundConstructInterceptionContext(aroundConstructInterceptionContext);
    }

    public JerseyClientCreationalContext setInjectionManager(CdiClientInjectionManager injectionManager) {
        this.injectionManager = injectionManager;
        return this;
    }

    public CdiClientInjectionManager getInjectionManager() {
        return injectionManager;
    }
}
