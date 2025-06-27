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

package org.glassfish.jersey.netty.connector;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.http.HttpHeaders;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The HTTP Redirect logic implementation for Netty Connector.
 *
 * @since 2.47
 */
public class NettyHttpRedirectController {

    private NettyConnectorProvider.Config.RW configuration;

    /* package */ void init(NettyConnectorProvider.Config.RW configuration) {
        this.configuration = configuration;
    }

    /**
     * Configure the HTTP request after HTTP Redirect response has been received.
     * By default, the HTTP POST request is not transformed into HTTP GET for status 301 & 302.
     * Also, HTTP Headers described by RFC 9110 Section 15.4 are removed from the new HTTP Request.
     *
     * @param request The new {@link ClientRequest} to be sent to the redirected URI.
     * @param response The original HTTP redirect {@link ClientResponse} received.
     * @return {@code true} when the new request should be sent.
     */
    public boolean prepareRedirect(ClientRequest request, ClientResponse response) {
        final boolean keepMethod = configuration.preserveMethodOnRedirect(request);

        if (Boolean.FALSE.equals(keepMethod) && request.getMethod().equals(HttpMethod.POST)) {
            switch (response.getStatus()) {
                case 301 /* MOVED PERMANENTLY */:
                case 302 /* FOUND */:
                    removeContentHeaders(request.getHeaders());
                    request.setMethod(HttpMethod.GET);
                    request.setEntity(null);
                    break;
            }
        }

        restrictRequestHeaders(request, response);
        return true;
    }

    /**
     * RFC 9110 Section 15.4 defines the HTTP headers that should be removed from the redirected request.
     * https://httpwg.org/specs/rfc9110.html#rfc.section.15.4.
     *
     * @param request the new request to a new URI location.
     * @param response the HTTP redirect response.
     */
    protected void restrictRequestHeaders(ClientRequest request, ClientResponse response) {
        final MultivaluedMap<String, Object> headers = request.getHeaders();

        for (final Iterator<Map.Entry<String, List<Object>>> it = headers.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry<String, List<Object>> entry = it.next();
            if (JerseyClientHandler.ProxyHeaders.INSTANCE.test(entry.getKey())) {
                it.remove();
            }
        }

        headers.remove(HttpHeaders.IF_MATCH);
        headers.remove(HttpHeaders.IF_NONE_MATCH);
        headers.remove(HttpHeaders.IF_MODIFIED_SINCE);
        headers.remove(HttpHeaders.IF_UNMODIFIED_SINCE);
        headers.remove(HttpHeaders.AUTHORIZATION);
        headers.remove(HttpHeaders.REFERER);
        headers.remove(HttpHeaders.COOKIE);
    }

    private void removeContentHeaders(MultivaluedMap<String, Object> headers) {
        for (final Iterator<Map.Entry<String, List<Object>>> it = headers.entrySet().iterator(); it.hasNext(); ) {
            final Map.Entry<String, List<Object>> entry = it.next();
            final String lowName = entry.getKey().toLowerCase(Locale.ROOT);
            if (lowName.startsWith("content-")) {
                it.remove();
            }
        }
        headers.remove(HttpHeaders.LAST_MODIFIED);
        headers.remove(HttpHeaders.TRANSFER_ENCODING);
    }

}
