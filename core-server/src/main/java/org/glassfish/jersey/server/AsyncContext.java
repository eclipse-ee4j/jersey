/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.util.Producer;

/**
 * Injectable asynchronous processing context that can be used to control various aspects
 * of asynchronous processing of a single request.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface AsyncContext extends AsyncResponse {

    /**
     * Asynchronous processing context state.
     */
    public static enum State {

        /**
         * Indicates the asynchronous processing context is running. This is a default state
         * the processing context is in case the processing execution flow has not been explicitly
         * modified (yet).
         */
        RUNNING,
        /**
         * Indicates the asynchronous processing context has been suspended.
         *
         * @see AsyncContext#suspend()
         */
        SUSPENDED,
        /**
         * Indicates the asynchronous processing context has been resumed.
         */
        RESUMED,
        /**
         * Indicates the processing represented by this asynchronous processing context
         * has been completed.
         */
        COMPLETED,
    }

    /**
     * Suspend the current asynchronous processing context.
     *
     * The method returns {@code true} if the context has been successfully suspended,
     * {@code false} otherwise.
     *
     * @return {@code true} if the request processing has been suspended successfully suspended,
     *         {@code false} otherwise.
     */
    public boolean suspend();

    /**
     * Invoke the provided response producer in a Jersey-managed asynchronous thread.
     *
     * @param producer response producer.
     */
    public void invokeManaged(Producer<Response> producer);
}
