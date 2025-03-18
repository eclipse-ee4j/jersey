/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.authentication;

import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BasicAuthenticatorTest {

    @Test
    void filterResponseAndAuthenticateNoAuthHeadersTest() {
        final BasicAuthenticator authenticator
                = new BasicAuthenticator(new HttpAuthenticationFilter.Credentials("foo", "bar"));
        final ClientRequestContext request = mock(ClientRequestContext.class);
        final ClientResponseContext response = mock(ClientResponseContext.class);

        when(response.getHeaders()).thenReturn(mock(MultivaluedMap.class));

        assertFalse(authenticator.filterResponseAndAuthenticate(request, response));
    }

    @Test
    void filterResponseAndAuthenticateAuthHeaderNotBasicTest() {
        final BasicAuthenticator authenticator
                = new BasicAuthenticator(new HttpAuthenticationFilter.Credentials("foo", "bar"));
        final ClientRequestContext request = mock(ClientRequestContext.class);
        final ClientResponseContext response = mock(ClientResponseContext.class);

        final MultivaluedMap<String, String> headers = mock(MultivaluedMap.class);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get(HttpHeaders.WWW_AUTHENTICATE)).thenReturn(Collections.singletonList("Digest realm=\"test\""));

        assertFalse(authenticator.filterResponseAndAuthenticate(request, response));
    }

    @Test
    void filterResponseAndAuthenticateEmptyListTest() {
        final BasicAuthenticator authenticator
                = new BasicAuthenticator(new HttpAuthenticationFilter.Credentials("foo", "bar"));
        final ClientRequestContext request = mock(ClientRequestContext.class);
        final ClientResponseContext response = mock(ClientResponseContext.class);

        final MultivaluedMap<String, String> headers = mock(MultivaluedMap.class);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get(HttpHeaders.WWW_AUTHENTICATE)).thenReturn(Collections.emptyList());

        assertFalse(authenticator.filterResponseAndAuthenticate(request, response));
    }

    @Test
    void filterResponseAndAuthenticateNullListTest() {
        final BasicAuthenticator authenticator
                = new BasicAuthenticator(new HttpAuthenticationFilter.Credentials("foo", "bar"));
        final ClientRequestContext request = mock(ClientRequestContext.class);
        final ClientResponseContext response = mock(ClientResponseContext.class);

        final MultivaluedMap<String, String> headers = mock(MultivaluedMap.class);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get(HttpHeaders.WWW_AUTHENTICATE)).thenReturn(null);

        assertFalse(authenticator.filterResponseAndAuthenticate(request, response));
    }

    @Test
    void filterResponseAndAuthenticateMissingCredentialsMultipleAuthRealmsTest() {
        final String[] authHeaders = new String[] {
                "Digest realm=\"test\"",
                "Basic realm=\"test\""
        };
        final BasicAuthenticator authenticator = new BasicAuthenticator(null);
        final ClientRequestContext request = mock(ClientRequestContext.class);
        final ClientResponseContext response = mock(ClientResponseContext.class);

        final MultivaluedMap<String, String> headers = mock(MultivaluedMap.class);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get(HttpHeaders.WWW_AUTHENTICATE)).thenReturn(Arrays.asList(authHeaders));
        when(response.hasEntity()).thenReturn(false);

        assertThrows(ResponseAuthenticationException.class,
                () -> authenticator.filterResponseAndAuthenticate(request, response));
    }

}
