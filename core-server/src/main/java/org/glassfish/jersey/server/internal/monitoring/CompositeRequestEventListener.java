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

import java.util.List;

import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/**
 * {@link RequestEventListener request event listener} that aggregates more request event listener into one.
 * Calling {@link RequestEventListener#onEvent(org.glassfish.jersey.server.monitoring.RequestEvent)} on
 * this composite listener will forward calls to all aggregated listeners.
 *
 * @author Miroslav Fuksa
 */
public class CompositeRequestEventListener implements RequestEventListener {

    private final List<RequestEventListener> requestEventListeners;

    /**
     * Create a new composite listener.
     *
     * @param requestEventListeners List of listeners that should be aggregated.
     */
    public CompositeRequestEventListener(List<RequestEventListener> requestEventListeners) {
        this.requestEventListeners = requestEventListeners;
    }

    @Override
    public void onEvent(RequestEvent event) {
        for (RequestEventListener requestEventListener : requestEventListeners) {
            requestEventListener.onEvent(event);
        }
    }
}
