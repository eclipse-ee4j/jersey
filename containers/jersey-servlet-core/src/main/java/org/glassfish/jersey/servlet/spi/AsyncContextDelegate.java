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

package org.glassfish.jersey.servlet.spi;

import org.glassfish.jersey.server.spi.ContainerResponseWriter;

/**
 * Utilized by the Servlet container response writer to deal with the container async features.
 * Individual instances are created by {@link AsyncContextDelegateProvider}.
 *
 * @see AsyncContextDelegateProvider
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public interface AsyncContextDelegate {

    /**
     * Invoked by the superior {@link ContainerResponseWriter} responsible for writing the response when processing is to be
     * suspended. An implementation can throw an {@link UnsupportedOperationException} if suspend is not supported (the default
     * behavior).
     *
     * @see ContainerResponseWriter#suspend(long, java.util.concurrent.TimeUnit, org.glassfish.jersey.server.spi.ContainerResponseWriter.TimeoutHandler)
     * @throws IllegalStateException if underlying {@link javax.servlet.ServletRequest servlet request} throws an exception.
     */
    public void suspend() throws IllegalStateException;

    /**
     * Invoked upon a response writing completion when the response write is either committed or canceled.
     */
    public void complete();
}
