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

package org.glassfish.jersey.client.oauth2;

import javax.ws.rs.client.Client;

/**
 * Abstract implementation of {@link OAuth2CodeGrantFlow.Builder}.
 *
 * @author Miroslav Fuksa
 * @since 2.3
 */
class AbstractAuthorizationCodeGrantBuilder<T extends OAuth2CodeGrantFlow.Builder> implements OAuth2CodeGrantFlow.Builder<T> {

    private final OAuth2CodeGrantFlow.Builder<T> delegate;

    public AbstractAuthorizationCodeGrantBuilder(OAuth2CodeGrantFlow.Builder<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T accessTokenUri(String accessTokenUri) {
        delegate.accessTokenUri(accessTokenUri);
        return (T) this;
    }

    @Override
    public T authorizationUri(String authorizationUri) {
        delegate.authorizationUri(authorizationUri);
        return (T) this;
    }

    @Override
    public T redirectUri(String redirectUri) {
        delegate.redirectUri(redirectUri);
        return (T) this;
    }

    @Override
    public T clientIdentifier(ClientIdentifier consumerCredentials) {
        delegate.clientIdentifier(consumerCredentials);
        return (T) this;
    }

    @Override
    public T scope(String scope) {
        delegate.scope(scope);
        return (T) this;
    }

    @Override
    public T client(Client client) {
        delegate.client(client);
        return (T) this;
    }

    @Override
    public T refreshTokenUri(String refreshTokenUri) {
        delegate.refreshTokenUri(refreshTokenUri);
        return (T) this;
    }

    @Override
    public T property(OAuth2CodeGrantFlow.Phase phase, String key, String value) {
        delegate.property(phase, key, value);
        return (T) this;
    }

    @Override
    public OAuth2CodeGrantFlow build() {
        return delegate.build();
    }
}
