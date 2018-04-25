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

import java.io.Closeable;

/**
 * A closeable service to add instances of {@link Closeable} that
 * are required to be closed.
 * <p>
 * This interface may be injected onto server-side components using
 * the {@link javax.ws.rs.core.Context} annotation.
 * <p>
 * The service may be used within the scope of a request to add instances
 * of {@link Closeable} that are to be closed when the request goes out
 * of scope, more specifically after the request has been processed and the
 * response has been returned.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Paul Sandoz
 */
public interface CloseableService {

    /**
     * Register a new instance of {@link Closeable} that is to be closed when the request goes out of scope.
     * <p>
     * After {@link #close()} has been called, this method will not accept any new instance registrations and
     * will return {@code false} instead.
     * </p>
     *
     * @param c the instance of {@link Closeable}.
     * @return {@code true} if the closeable service has not been closed yet and the closeable instance was successfully
     * registered with the service, {@code false} otherwise.
     */
    public boolean add(Closeable c);

    /**
     * Invokes {@code Closeable#close()} method on all instances of {@link Closeable} added by the {@code #add(Closeable)}
     * method.
     * Subsequent calls of this method should not do anything.
     */
    public void close();
}
