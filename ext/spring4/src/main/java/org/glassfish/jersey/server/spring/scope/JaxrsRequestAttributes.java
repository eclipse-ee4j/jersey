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

import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.jersey.server.spring.LocalizationMessages;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.AbstractRequestAttributes;

/**
 * JAX-RS based Spring RequestAttributes implementation.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class JaxrsRequestAttributes extends AbstractRequestAttributes {

    private final ContainerRequestContext requestContext;

    /**
     * Create a new instance.
     *
     * @param requestContext JAX-RS container request context
     */
    public JaxrsRequestAttributes(ContainerRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    protected void updateAccessedSessionAttributes() {
        // sessions not supported
    }

    @Override
    public Object getAttribute(String name, int scope) {
        return requestContext.getProperty(name);
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        requestContext.setProperty(name, value);
    }

    @Override
    public void removeAttribute(String name, int scope) {
        requestContext.removeProperty(name);
    }

    @Override
    public String[] getAttributeNames(int scope) {
        if (!isRequestActive()) {
            throw new IllegalStateException(LocalizationMessages.NOT_IN_REQUEST_SCOPE());
        }
        return StringUtils.toStringArray(requestContext.getPropertyNames());
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback, int scope) {
        registerRequestDestructionCallback(name, callback);
    }

    @Override
    public Object resolveReference(String key) {
        if (REFERENCE_REQUEST.equals(key)) {
            return requestContext;
        }
        return null;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public Object getSessionMutex() {
        return null;
    }
}
