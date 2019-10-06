/*
 * Copyright (c) 2018 Markus KARG. All rights reserved.
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

package org.glassfish.jersey.server.spi;

import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.spi.Contract;

/**
 * Jersey service contract for self-contained servers.
 * <p>
 * Runs a self-contained {@link Application} in a {@link Container} using an
 * HTTP server implicitly started and stopped together with the application.
 * </p>
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.30
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface Server {

    /**
     * @return container in which the application lives.
     */
    public Container container();

    /**
     * @return IP port the application listens to for requests.
     */
    public int port();

    /**
     * Initiates server bootstrap.
     * <p>
     * Startup happens in background. The completion stage produces a native startup
     * result.
     * </p>
     * <p>
     * Portable applications should not expect any particular result type, as it is
     * implementation-specific.
     * </p>
     *
     * @return A {@link CompletionStage} providing a native startup result of the
     *         bootstrap process. The native result MAY be {@code null}.
     */
    public CompletionStage<?> start();

    /**
     * Initiates server shutdown.
     * <p>
     * Shutdown happens in background. The completion stage produces a native
     * shutdown result.
     * </p>
     * </p>
     * Portable applications should not expect any particular result type, as it is
     * implementation-specific.
     * </p>
     *
     * @return A {@link CompletionStage} providing a native shutdown result of the
     *         shutdown process. The native result MAY be {@code null}.
     */
    public CompletionStage<?> stop();

    /**
     * Provides access to the native handle(s) of the server, if it holds at least
     * one.
     * <p>
     * Implementations MAY use native handles to identify the server instance, and /
     * or use those to communicate with and control the instance. Whether or not
     * such handles exist, and their respective data types, is
     * implementation-specific.
     * </p>
     * <p>
     * Portable applications should not invoke this method, as the types of
     * supported handles are implementation-specific.
     * </p>
     *
     * @param nativeClass
     *            The class of the native handle.
     * @return The native handle, or {@code null} if no handle of this type exists.
     */
    public <T> T unwrap(Class<T> nativeClass);

}
