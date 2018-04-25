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
 * Callback notification mechanism that signals to the developer it's possible
 * to write content without blocking.
 * <p/>
 * Based on Servlet 3.1
 */
interface WriteListener {

    /**
     * When an instance of the WriteListener is registered with a {@link BodyOutputStream},
     * this method will be invoked by the container the first time when it is possible
     * to write data. Subsequently the container will invoke this method if and only
     * if {@link BodyOutputStream#isReady()} method
     * has been called and has returned <code>false</code>.
     *
     * @throws IOException if an I/O related error has occurred during processing
     */
    void onWritePossible() throws IOException;

    /**
     * Invoked when an error occurs writing data using the non-blocking APIs.
     */
    void onError(final Throwable t);
}
