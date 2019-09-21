/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.monitoring;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

/**
 * Container listener that listens to container events and trigger the {@link ApplicationEvent application events}
 * and call them on supplied {@link org.glassfish.jersey.server.monitoring.RequestEventListener}.
 * <p/>
 * This listener must be registered as a standard provider in Jersey runtime.
 *
 * @author Miroslav Fuksa
 */
public final class MonitoringContainerListener implements ContainerLifecycleListener {

    private volatile ApplicationEvent initFinishedEvent;
    private volatile ApplicationEventListener listener;

    /**
     * Initializes the instance with listener that must be called and initialization event. If this method
     * is not called then events cannot not be triggered which might be needed when no
     * {@link ApplicationEventListener} is registered in Jersey runtime.
     *
     * @param listener Listener that should be called.
     * @param initFinishedEvent Event of type {@link ApplicationEvent.Type#INITIALIZATION_START}.
     */
    public void init(ApplicationEventListener listener, ApplicationEvent initFinishedEvent) {
        this.listener = listener;
        this.initFinishedEvent = initFinishedEvent;
    }

    @Override
    public void onStartup(Container container) {
        if (listener != null) {
            listener.onEvent(getApplicationEvent(ApplicationEvent.Type.INITIALIZATION_FINISHED));
        }
    }

    @Override
    public void onReload(Container container) {
        if (listener != null) {
            listener.onEvent(getApplicationEvent(ApplicationEvent.Type.RELOAD_FINISHED));
        }
    }

    private ApplicationEvent getApplicationEvent(ApplicationEvent.Type type) {
        return new ApplicationEventImpl(type,
                initFinishedEvent.getResourceConfig(), initFinishedEvent.getProviders(),
                initFinishedEvent.getRegisteredClasses(), initFinishedEvent.getRegisteredInstances(),
                initFinishedEvent.getResourceModel());
    }

    @Override
    public void onShutdown(Container container) {
        if (listener != null) {
            listener.onEvent(getApplicationEvent(ApplicationEvent.Type.DESTROY_FINISHED));
        }
    }

    /**
     * A binder that binds the {@link MonitoringContainerListener}.
     */
    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bindAsContract(MonitoringContainerListener.class)
                    .to(ContainerLifecycleListener.class).in(Singleton.class);
        }
    }
}
