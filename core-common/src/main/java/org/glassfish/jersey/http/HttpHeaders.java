/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.http;

/**
 * Additional HTTP headers that are not listed in Jakarta REST {@link jakarta.ws.rs.core.HttpHeaders}.
 */
public interface HttpHeaders extends jakarta.ws.rs.core.HttpHeaders {

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-accept-ranges">HTTP Semantics documentation</a>}
     */
    public static final String ACCEPT_RANGES = "Accept-Ranges";

    /**
     * See {@link <a href="https://www.ietf.org/rfc/rfc5789.txt">PATCH Method for HTTP</a>}
     */
    public static final String ACCEPT_PATCH = "Accept-Patch";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9111#field.age">HTTP Caching</a>}
     */
    public static final String AGE = "Age";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-connection">HTTP Semantics documentation</a>}
     */
    public static final String CONNECTION = "Connection";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-content-range">HTTP Semantics documentation</a>}
     */
    public static final String CONTENT_RANGE = "Content-Range";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-expect">HTTP Semantics documentation</a>}
     */
    public static final String EXPECT = "Expect";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc7239.html">Forwarded HTTP Extension</a>}
     */
    public static final String FORWARDED = "Forwarded";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-from">HTTP Semantics documentation</a>}
     */
    public static final String FROM = "From";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-if-range">HTTP Semantics documentation</a>}
     */
    public static final String IF_RANGE = "If-Range";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-max-forwards">HTTP Semantics documentation</a>}
     */
    public static final String MAX_FORWARDS = "Max-Forwards";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc2045.txt">(MIME) Part One: Format of Internet Message Bodies</a>}
     */
    public static final String MIME_VERSION = "Mime-Version";

    /**
     * See {@link <a href="https://datatracker.ietf.org/doc/html/rfc8288">Web Linking</a>}
     */
    public static final String LINK = "Link";

    /**
     * See {@link <a href="https://datatracker.ietf.org/doc/html/rfc6454">The Web Origin Concept</a>}
     */
    public static final String ORIGIN = "Origin";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-proxy-authenticate">HTTP Semantics documentation</a>}
     */
    public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-proxy-authorization">HTTP Semantics documentation</a>}
     */
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-proxy-authentication-info">HTTP Semantics documentation</a>}
     */
    public static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9112.html#name-keep-alive-connections">HTTP/1.1 documentation</a>}
     */
    public static final String PROXY_CONNECTION = "Proxy-Connection";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-range">HTTP Semantics documentation</a>}
     */
    public static final String RANGE = "Range";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-referer">HTTP Semantics documentation</a>}
     */
    public static final String REFERER = "Referer";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-server">HTTP Semantics documentation</a>}
     */
    public static final String SERVER = "Server";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-te">HTTP Semantics documentation</a>}
     */
    public static final String TE = "TE";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-trailer">HTTP Semantics documentation</a>}
     */
    public static final String TRAILER = "Trailer";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9112#name-transfer-encoding">HTTP Semantics documentation</a>}
     */
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-upgrade">HTTP Semantics documentation</a>}
     */
    public static final String UPGRADE = "Upgrade";

    /**
     * See {@link <a href="https://www.rfc-editor.org/rfc/rfc9110#name-via">HTTP Semantics documentation</a>}
     */
    public static final String VIA = "Via";
}
