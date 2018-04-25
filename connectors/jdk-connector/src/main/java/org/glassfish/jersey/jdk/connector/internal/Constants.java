/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jdk.connector.internal;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
class Constants {

    static final String CONNECTION = "Connection";
    static final String CONNECTION_CLOSE = "Close";
    static final String HTTPS = "https";
    static String TRANSFER_ENCODING_HEADER = "Transfer-Encoding";
    static String TRANSFER_ENCODING_CHUNKED = "chunked";
    static String CONTENT_LENGTH = "Content-Length";
    static String HOST = "Host";
    static final String HEAD = "HEAD";
    static final String CONNECT = "CONNECT";
    static final String GET = "GET";
    static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
    static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
    static final String PROXY_CONNECTION = "ProxyConnection";
    static final String KEEP_ALIVE = "keep-alive";
    /**
     * Basic authentication scheme key.
     */
    static final String BASIC = "Basic";

    /**
     * Digest authentication scheme key.
     */
    static final String DIGEST = "Digest";
}
