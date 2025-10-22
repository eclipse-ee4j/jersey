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

package org.glassfish.jersey.constants.http;

/**
 * This is a list of Hypertext Transfer Protocol (HTTP) response status codes.
 * The Internet Assigned Numbers Authority (IANA) maintains the official registry of HTTP status codes.
 * See <a href="https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml">Hypertext Transfer Protocol (HTTP) Status Code Registry</a>.
 */
public final class ResponseStatus {

    /**
     * 1xx informational status codes - request received, continuing process
     */
    public static class Info1xx extends org.glassfish.jersey.http.ResponseStatus.Info1xx {
    }

    /**
     * 2xx success status codes - the action was successfully received, understood, and accepted.
     */
    public static class Success2xx extends org.glassfish.jersey.http.ResponseStatus.Success2xx {
    }

    /**
     * 3xx redirection status codes - further action must be taken in order to complete the request.
     */
    public static class Redirect3xx extends org.glassfish.jersey.http.ResponseStatus.Redirect3xx {
    }

    /**
     * 4xx client error status codes - the request contains bad syntax or cannot be fulfilled.
     */
    public static class ClientError4xx extends org.glassfish.jersey.http.ResponseStatus.Redirect3xx {
    }

    /**
     * 5xx server error status codes - the server failed to fulfill an apparently valid request.
     */
    public static class ServerError5xx extends  org.glassfish.jersey.http.ResponseStatus.ServerError5xx {
    }
}
