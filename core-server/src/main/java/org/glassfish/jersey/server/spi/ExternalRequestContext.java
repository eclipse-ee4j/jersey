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

package org.glassfish.jersey.server.spi;

/**
 * Wrapper for externally provided request context data.
 *
 * @param <T> external request data.
 *
 * @see ExternalRequestScope
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class ExternalRequestContext<T> {

    private final T context;

    /**
     * Create new external request context containing given data.
     *
     * @param context external context data.
     */
    public ExternalRequestContext(T context) {
        this.context = context;
    }

    /**
     * Get me current external context data.
     *
     * @return context data.
     */
    public T getContext() {
        return context;
    }
}
