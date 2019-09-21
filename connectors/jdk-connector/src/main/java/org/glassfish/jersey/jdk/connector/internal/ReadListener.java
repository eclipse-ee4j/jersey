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

import java.io.IOException;

/**
 * <p>
 * This class represents a call-back mechanism that will notify implementations
 * as HTTP request data becomes available to be read without blocking.
 * </p>
 * <p/>
 * Taken from Servlet 3.1
 */
interface ReadListener {

    /**
     * When an instance of the <code>ReadListener</code> is registered with a {@link BodyInputStream},
     * this method will be invoked by the container the first time when it is possible
     * to read data. Subsequently the container will invoke this method if and only
     * if {@link BodyInputStream#isReady()} method
     * has been called and has returned <code>false</code>.
     *
     * @throws IOException if an I/O related error has occurred during processing
     */
    void onDataAvailable() throws IOException;

    /**
     * Invoked when all data for the current request has been read.
     *
     * @throws IOException if an I/O related error has occurred during processing
     */

    void onAllDataRead() throws IOException;

    /**
     * Invoked when an error occurs processing the request.
     */
    void onError(Throwable t);
}
