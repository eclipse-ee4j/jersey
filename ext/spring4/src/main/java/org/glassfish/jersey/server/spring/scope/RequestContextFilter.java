/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spring.scope;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.glassfish.jersey.internal.inject.InjectionManager;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.AbstractRequestAttributes;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Spring filter to provide a bridge between JAX-RS and Spring request attributes.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Provider
@PreMatching
public final class RequestContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String REQUEST_ATTRIBUTES_PROPERTY = RequestContextFilter.class.getName() + ".REQUEST_ATTRIBUTES";

    private final SpringAttributeController attributeController;

    private static final SpringAttributeController EMPTY_ATTRIBUTE_CONTROLLER = new SpringAttributeController() {
        @Override
        public void setAttributes(final ContainerRequestContext requestContext) {
        }

        @Override
        public void resetAttributes(final ContainerRequestContext requestContext) {
        }
    };

    private interface SpringAttributeController {

        void setAttributes(final ContainerRequestContext requestContext);

        void resetAttributes(final ContainerRequestContext requestContext);
    }

    /**
     * Create a new request context filter instance.
     *
     * @param injectionManager injection manager.
     */
    @Inject
    public RequestContextFilter(final InjectionManager injectionManager) {
        final ApplicationContext appCtx = injectionManager.getInstance(ApplicationContext.class);
        final boolean isWebApp = appCtx instanceof WebApplicationContext;

        attributeController = appCtx != null ? new SpringAttributeController() {

            @Override
            public void setAttributes(final ContainerRequestContext requestContext) {
                final RequestAttributes attributes;
                if (isWebApp) {
                    final HttpServletRequest httpRequest = injectionManager.getInstance(HttpServletRequest.class);
                    attributes = new JaxrsServletRequestAttributes(httpRequest, requestContext);
                } else {
                    attributes = new JaxrsRequestAttributes(requestContext);
                }
                requestContext.setProperty(REQUEST_ATTRIBUTES_PROPERTY, attributes);
                RequestContextHolder.setRequestAttributes(attributes);
            }

            @Override
            public void resetAttributes(final ContainerRequestContext requestContext) {
                final AbstractRequestAttributes attributes =
                        (AbstractRequestAttributes) requestContext.getProperty(REQUEST_ATTRIBUTES_PROPERTY);
                RequestContextHolder.resetRequestAttributes();
                attributes.requestCompleted();
            }
        } : EMPTY_ATTRIBUTE_CONTROLLER;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        attributeController.setAttributes(requestContext);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {
        attributeController.resetAttributes(requestContext);
    }
}
