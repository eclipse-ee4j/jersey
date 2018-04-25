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

package org.glassfish.jersey.server.internal.process;

import java.security.Principal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import javax.inject.Inject;

import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * Proxiable wrapper for request scoped {@link SecurityContext} instance.
 *
 * <p>
 * This wrapper must be used and cannot be replaced by {@link org.glassfish.jersey.internal.inject.ReferencingFactory}.
 * The reason is that {@link SecurityContext security context} can be set
 * many times during the request processing. However, the HK2 proxy caches
 * the first value that is injected. So, if for example any filter injects
 * security context, then this security context will be cached and it will
 * never be replaced for the same request. On the other hand, HK2 should
 * probably cache the first value returned in the request scope to prevent
 * that two subsequent calls done on the proxy will be forwarded to different
 * object if the the object changes in the meantime.
 * <p/>
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
class SecurityContextInjectee implements SecurityContext {

    private final ContainerRequestContext requestContext;

    /**
     * Injection constructor.
     *
     * @param requestContext {@code SecurityContext} source.
     */
    @Inject
    public SecurityContextInjectee(ContainerRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public Principal getUserPrincipal() {
        checkState();
        return requestContext.getSecurityContext().getUserPrincipal();
    }

    @Override
    public boolean isUserInRole(String role) {
        checkState();
        return requestContext.getSecurityContext().isUserInRole(role);
    }

    @Override
    public boolean isSecure() {
        checkState();
        return requestContext.getSecurityContext().isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        checkState();
        return requestContext.getSecurityContext().getAuthenticationScheme();
    }

    @Override
    public int hashCode() {
        checkState();
        return 7 * requestContext.getSecurityContext().hashCode();
    }

    @Override
    public boolean equals(Object that) {
        checkState();

        return that instanceof SecurityContext && that.equals(requestContext.getSecurityContext());
    }

    private void checkState() {
        if (requestContext == null) {
            throw new IllegalStateException(LocalizationMessages.SECURITY_CONTEXT_WAS_NOT_SET());
        }
    }
}
