/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.ApplicationInfo;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Date;

/**
 * {@link ApplicationEventListener Application event listener} that listens to {@link ApplicationEvent application}
 * events and just prepare {@link ApplicationInfo} instance to be injectable.
 *
 * @see MonitoringEventListener
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@Priority(ApplicationInfoListener.PRIORITY)
public final class ApplicationInfoListener implements ApplicationEventListener {

    public static final int PRIORITY = 1000;

    @Inject
    private Provider<Ref<ApplicationInfo>> applicationInfoRefProvider;

    @Override
    public RequestEventListener onRequest(final RequestEvent requestEvent) {
        return null;
    }

    @Override
    public void onEvent(final ApplicationEvent event) {
        final ApplicationEvent.Type type = event.getType();
        switch (type) {
            case RELOAD_FINISHED:
            case INITIALIZATION_FINISHED:
                processApplicationStatistics(event);
                break;
        }
    }

    private void processApplicationStatistics(ApplicationEvent event) {
        final long now = System.currentTimeMillis();
        final ApplicationInfo applicationInfo = new ApplicationInfoImpl(event.getResourceConfig(),
                new Date(now), event.getRegisteredClasses(),
                event.getRegisteredInstances(), event.getProviders());
        applicationInfoRefProvider.get().set(applicationInfo);
    }

}
