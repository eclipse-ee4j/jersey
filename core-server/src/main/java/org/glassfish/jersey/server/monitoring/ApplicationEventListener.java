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

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.spi.Contract;

/**
 * Jersey specific provider that listens to {@link ApplicationEvent application events}.
 * The implementation of this interface will be called for two kind of events:
 * application events and {@link RequestEvent request events}. This interface will listen to
 * all {@link org.glassfish.jersey.server.monitoring.ApplicationEvent.Type application event types}
 * but only to first request event which is the {@link RequestEvent.Type#START}. On this event the
 * application event listener can decide whether it will listen to the request and return {@link RequestEventListener
 * request event listener} for listening to further request events.
 * }
 * <p/>
 * The implementation of this interface can be registered as a standard Jersey/JAX-RS provider
 * by annotating with {@link javax.ws.rs.ext.Provider @Provider} annotation in the case of
 * class path scanning, by registering as a provider using {@link org.glassfish.jersey.server.ResourceConfig}
 * or by returning from {@link javax.ws.rs.core.Application#getClasses()}
 * or {@link javax.ws.rs.core.Application#getSingletons()}}. The provider can be registered only on the server
 * side.
 * <p/>
 * Application event listener can read data of events but must not modify them in any way. The implementation
 * must be thread safe (the methods might be called from different threads).
 *
 * @author Miroslav Fuksa
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ApplicationEventListener {
    /**
     * Process the application {@code event}. This method is called when new event occurs.
     *
     * @param event Application event.
     */
    public void onEvent(ApplicationEvent event);

    /**
     * Process a new request and return a {@link RequestEventListener request event listener} if
     * listening to {@link RequestEvent request events} is required. The method is called once for
     * each new incoming request. If listening to the request is required then request event must be returned
     * from the method. Such a request event listener will receive all request events that one request. If listening
     * to request event for the request is not required then {@code null} must be returned
     * from the method (do not return empty mock listener in these
     * cases as it will have negative performance impact).
     *
     * @param requestEvent Event of type {@link RequestEvent.Type#START}.
     * @return Request event listener that will monitor the events of the request
     *         connected with {@code requestEvent}; null otherwise.
     */
    public RequestEventListener onRequest(RequestEvent requestEvent);
}
