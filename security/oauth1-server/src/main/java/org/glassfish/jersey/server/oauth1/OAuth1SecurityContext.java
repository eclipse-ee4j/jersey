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

package org.glassfish.jersey.server.oauth1;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.oauth1.signature.OAuth1Parameters;

/**
 * Security request that gets injected into the context by the OAuth filter
 * based on the access token attached to the request.
 *
 * @author Martin Matula
 */
class OAuth1SecurityContext implements SecurityContext {
    private final OAuth1Consumer consumer;
    private final OAuth1Token token;
    private final boolean isSecure;

    /**
     * Create a new OAuth security context from {@link OAuth1Consumer consumer}.
     *
     * @param consumer OAuth consumer for which the context will be created.
     * @param isSecure {@code true} if the request is secured over SSL (HTTPS).
     */
    public OAuth1SecurityContext(OAuth1Consumer consumer, boolean isSecure) {
        this.consumer = consumer;
        this.token = null;
        this.isSecure = isSecure;
    }

    /**
     * Create a new OAuth security context from {@link OAuth1Token Access Token}.
     * @param token Access Token.
     * @param isSecure {@code true} if the request is secured over SSL (HTTPS).
     */
    public OAuth1SecurityContext(OAuth1Token token, boolean isSecure) {
        this.consumer = null;
        this.token = token;
        this.isSecure = isSecure;
    }

    @Override
    public Principal getUserPrincipal() {
        return consumer == null ? token.getPrincipal() : consumer.getPrincipal();
    }

    @Override
    public boolean isUserInRole(String string) {
        return consumer == null ? token.isInRole(string) : consumer.isInRole(string);
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
        return OAuth1Parameters.SCHEME;
    }

}
