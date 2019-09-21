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

import org.glassfish.jersey.internal.util.collection.NonBlockingInputStream;

/**
 * TODO consider exposing the mode as part of the API, so the user can make decisions based on the mode
 * <p/>
 * An extension of {@link NonBlockingInputStream} that adds methods that enable using the stream asynchronously.
 * The asynchronous mode is inspired by and works in a very similar way as Servlet asynchronous streams introduced in Servlet
 * 3.1.
 * <p/>
 * The stream supports 2 modes of operation SYNCHRONOUS and ASYNCHRONOUS.
 * The stream is one of the following 3 states:
 * <ul>
 * <li>UNDECIDED</li>
 * <li>SYNCHRONOUS</li>
 * <li>ASYNCHRONOUS</li>
 * </ul>
 * UNDECIDED is an initial mode and it commits either to SYNCHRONOUS or ASYNCHRONOUS. Once it commits to one of these
 * 2 modes it cannot change to the other. The mode it commits to is decided based on the first use of te stream.
 * If {@link #setReadListener(ReadListener)} is invoked before any of the read or tryRead methods, it commits to ASYNCHRONOUS
 * mode and similarly if any of the read or tryRead methods is invoked before {@link #setReadListener(ReadListener)},
 * it commits to SYNCHRONOUS mode.
 */
abstract class BodyInputStream extends NonBlockingInputStream {

    /**
     * Returns true if data can be read without blocking else returns
     * false.
     * <p/>
     * If the stream is in ASYNCHRONOUS mode and the user attempts to read from it even though this method returns
     * false, an {@link IllegalStateException} is thrown.
     *
     * @return <code>true</code> if data can be obtained without blocking,
     * otherwise returns <code>false</code>.
     */
    public abstract boolean isReady();

    /**
     * Instructs the stream to invoke the provided {@link ReadListener} when it is possible to read.
     * <p/>
     * If the stream is in UNDECIDED state, invoking this method will commit the stream to ASYNCHRONOUS mode.
     *
     * @param readListener the {@link ReadListener} that should be notified
     *                     when it's possible to read.
     * @throws IllegalStateException if one of the following conditions is true
     *                               <ul>
     *                               <li>the stream has already committed to  SYNCHRONOUS mode. <li/>
     *                               <li>setReadListener is called more than once within the scope of the same request. <li/>
     *                               </ul>
     * @throws NullPointerException  if readListener is null
     */
    public abstract void setReadListener(ReadListener readListener);
}
