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

package org.glassfish.jersey.server.monitoring;

/**
 * Non-registrable provider that listens to {@link RequestEvent request events}.
 * <p/>
 * The implementation of the interface will be called for request events when they occur. The provider
 * cannot be registered as a standard JAX-RS or Jersey provider. The instance of the
 * {@code RequestEventListener} must be returned from the {@link ApplicationEventListener#onRequest(RequestEvent)}.
 * This will register the instance for listening of request events for one particular request. Once
 * the processing of the request is finished, the instance will be ignored by the Jersey runtime and not used
 * for processing of further requests.
 *
 * @author Miroslav Fuksa
 * @see ApplicationEventListener for details of how to register the {@code RequestEventListener}.
 */
public interface RequestEventListener {
    /**
     * The method is called when new request event occurs. This method will never be called for method
     * {@link RequestEvent.Type#START} as this event is handled by {@link ApplicationEventListener}.
     *
     * @param event Request event.
     */
    public void onEvent(RequestEvent event);
}
