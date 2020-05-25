/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.mvc.jsp;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.glassfish.jersey.server.mvc.spi.ResolvedViewable;

/**
 * {@link RequestDispatcher Request dispatcher wrapper} for setting attributes (e.g. {@code it}).
 *
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
final class RequestDispatcherWrapper implements RequestDispatcher {

    static final String BASE_PATH_ATTRIBUTE_NAME = "_basePath";
    static final String OLD_MODEL_ATTRIBUTE_NAME = "it";
    static final String MODEL_ATTRIBUTE_NAME = "model";
    static final String RESOLVING_CLASS_ATTRIBUTE_NAME = "resolvingClass";
    static final String REQUEST_ATTRIBUTE_NAME = "_request";
    static final String RESPONSE_ATTRIBUTE_NAME = "_response";

    private final RequestDispatcher dispatcher;

    private final String basePath;

    private final ResolvedViewable viewable;

    /**
     * Creates new {@code RequestDispatcherWrapper} responsible for setting request attributes and forwarding the processing to
     * the given dispatcher.
     *
     * @param dispatcher dispatcher processing the request after all the request attributes were set.
     * @param basePath base path of all JSP set to {@value #BASE_PATH_ATTRIBUTE_NAME} request attribute.
     * @param viewable viewable to obtain model and resolving class from.
     */
    public RequestDispatcherWrapper(
            final RequestDispatcher dispatcher, final String basePath, final ResolvedViewable viewable) {
        this.dispatcher = dispatcher;
        this.basePath = basePath;
        this.viewable = viewable;
    }

    @Override
    public void forward(final ServletRequest request, final ServletResponse response) throws ServletException, IOException {
        final Object oldIt = request.getAttribute(MODEL_ATTRIBUTE_NAME);
        final Object oldResolvingClass = request.getAttribute(RESOLVING_CLASS_ATTRIBUTE_NAME);

        request.setAttribute(RESOLVING_CLASS_ATTRIBUTE_NAME, viewable.getResolvingClass());

        request.setAttribute(OLD_MODEL_ATTRIBUTE_NAME, viewable.getModel());
        request.setAttribute(MODEL_ATTRIBUTE_NAME, viewable.getModel());

        request.setAttribute(BASE_PATH_ATTRIBUTE_NAME, basePath);
        request.setAttribute(REQUEST_ATTRIBUTE_NAME, request);
        request.setAttribute(RESPONSE_ATTRIBUTE_NAME, response);

        dispatcher.forward(request, response);

        request.setAttribute(RESOLVING_CLASS_ATTRIBUTE_NAME, oldResolvingClass);
        request.setAttribute(MODEL_ATTRIBUTE_NAME, oldIt);
    }

    @Override
    public void include(final ServletRequest request, final ServletResponse response) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

}
