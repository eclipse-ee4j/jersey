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

import java.util.ArrayList;
import java.util.List;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/**
 * {@link ApplicationEventListener application event listener} that aggregates more event listeners into one.
 * Calling listener methods on this listener will cause calling methods on all aggregated listeners.
 *
 * @author Miroslav Fuksa
 */
public class CompositeApplicationEventListener implements ApplicationEventListener {

    private final Iterable<ApplicationEventListener> applicationEventListeners;

    /**
     * Creates a new instance of composite event listener.
     *
     * @param applicationEventListeners List of application event listener that should be aggregated.
     */
    public CompositeApplicationEventListener(final Iterable<ApplicationEventListener> applicationEventListeners) {
        this.applicationEventListeners = applicationEventListeners;
    }

    @Override
    public void onEvent(final ApplicationEvent event) {
        for (final ApplicationEventListener applicationEventListener : applicationEventListeners) {
            applicationEventListener.onEvent(event);
        }
    }

    @Override
    public RequestEventListener onRequest(final RequestEvent requestEvent) {
        final List<RequestEventListener> requestEventListeners = new ArrayList<>();
        for (final ApplicationEventListener applicationEventListener : applicationEventListeners) {
            final RequestEventListener requestEventListener = applicationEventListener.onRequest(requestEvent);
            if (requestEventListener != null) {
                requestEventListeners.add(requestEventListener);
            }
        }

        return requestEventListeners.isEmpty() ? null
                : new CompositeRequestEventListener(requestEventListeners);
    }
}
