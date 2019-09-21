/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

import java.io.OutputStream;

/**
 * TODO consider exposing the mode as part of the API, so the user can make decisions based on the mode
 * <p/>
 * An extension of {@link OutputStream} that adds method that allow to use the stream asynchronously.
 * It is inspired by and works in a very similar way as Servlet asynchronous streams introduced in Servlet 3.1.
 * <p/>
 * The stream supports 2 modes SYNCHRONOUS and ASYNCHRONOUS.
 * The stream is one of the following 3 states:
 * <ul>
 * <li>UNDECIDED</li>
 * <li>SYNCHRONOUS</li>
 * <li>ASYNCHRONOUS</li>
 * </ul>
 * UNDECIDED is an initial mode and it commits either to SYNCHRONOUS or ASYNCHRONOUS. Once it commits to one of these
 * 2 modes it cannot change to the other. The mode it commits to is decided based on the first use of the stream.
 * If {@link #setWriteListener(WriteListener)} is invoked before any of the write methods, it commits to ASYNCHRONOUS
 * mode and similarly if any of the write methods is invoked before {@link #setWriteListener(WriteListener)},
 * it commits to SYNCHRONOUS mode.
 */
abstract class BodyOutputStream extends OutputStream {

    /**
     * Instructs the stream to invoke the provided {@link WriteListener} when it is possible to write.
     * <p/>
     * If the stream is in UNDECIDED state, invoking this method will commit the stream to ASYNCHRONOUS mode.
     *
     * @param writeListener the {@link WriteListener} that should be notified
     *                      when it's possible to write.
     * @throws IllegalStateException if one of the following conditions is true
     *                               <ul>
     *                               <li>the stream has already committed to SYNCHRONOUS mode. <li/>
     *                               <li>setWriteListener is called more than once within the scope of the same request. <li/>
     *                               </ul>
     * @throws NullPointerException  if writeListener is null
     */
    public abstract void setWriteListener(WriteListener writeListener);

    /**
     * Returns true if data can be written without blocking else returns
     * false.
     * <p/>
     * If the stream is in ASYNCHRONOUS mode and the user attempts to write to it even though this method returns
     * false, an {@link IllegalStateException} is thrown.
     *
     * @return <code>true</code> if data can be obtained without blocking,
     * otherwise returns <code>false</code>.
     */
    public abstract boolean isReady();
}
