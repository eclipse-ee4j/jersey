/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.simple;

import java.io.Closeable;

/**
 * Simple server facade providing convenient methods to obtain info about the server (i.e. port).
 *
 * @author Michal Gajdos
 * @since 2.9
 */
public interface SimpleServer extends Closeable {

    /**
     * The port the server is listening to for incomming HTTP connections. If the port is not
     * specified the {@link org.glassfish.jersey.server.spi.Container.DEFAULT_PORT} is used.
     *
     * @return the port the server is listening on
     */
    public int getPort();

    /**
     * If this is true then very low level I/O operations are logged. Typically this is used to debug
     * I/O issues such as HTTPS handshakes or performance issues by analysing the various latencies
     * involved in the HTTP conversation.
     * <p/>
     * There is a minimal performance penalty if this is enabled and it is perfectly suited to being
     * enabled in a production environment, at the cost of logging overhead.
     *
     * @return {@code true} if debug is enabled, false otherwise.
     * @since 2.23
     */
    public boolean isDebug();

    /**
     * To enable very low level logging this can be enabled. This goes far beyond logging issues such
     * as connection establishment of request dispatch, it can trace the TCP operations latencies
     * involved.
     *
     * @param enable if {@code true} debug tracing will be enabled.
     * @since 2.23
     */
    public void setDebug(boolean enable);
}
