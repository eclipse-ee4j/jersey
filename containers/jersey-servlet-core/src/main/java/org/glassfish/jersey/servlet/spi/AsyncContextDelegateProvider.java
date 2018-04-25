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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Factory to create {@link AsyncContextDelegate} to deal with asynchronous
 * features added in Servlet version 3.0.
 * If no such a factory is registered via the {@code META-INF/services} mechanism
 * a default factory for Servlet versions prior to 3.0 will be utilized with no async support.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public interface AsyncContextDelegateProvider {

    /**
     * Factory method to create instances of Servlet container response writer extension,
     * {@link AsyncContextDelegate}, for response processing.
     *
     * @param request original request.
     * @param response original response.
     * @return an instance to be used throughout a single response write processing.
     */
    public AsyncContextDelegate createDelegate(final HttpServletRequest request, final HttpServletResponse response);
}
