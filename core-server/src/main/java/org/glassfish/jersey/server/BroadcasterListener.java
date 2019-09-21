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

/**
 * Listener interface that can be implemented to listen to events fired by {@link Broadcaster} object.
 *
 * To listen to events, implementation of this interface needs to register with a particular {@link Broadcaster} instance
 * using {@link Broadcaster#add(BroadcasterListener)}.
 *
 * @author Martin Matula
 */
public interface BroadcasterListener<T> {
    /**
     * Called when exception was thrown by a given chunked response when trying to write to it or close it.
     * @param chunkedOutput instance that threw exception
     * @param exception thrown exception
     */
    void onException(ChunkedOutput<T> chunkedOutput, Exception exception);

    /**
     * Called when the chunkedOutput has been closed (either by client closing the connection or by calling
     * {@link ChunkedOutput#close()} on the server side.
     *
     * @param chunkedOutput instance that has been closed.
     */
    void onClose(ChunkedOutput<T> chunkedOutput);
}
