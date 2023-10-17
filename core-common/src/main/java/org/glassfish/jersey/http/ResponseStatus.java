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

import javax.ws.rs.core.Response;

/**
 * This is a list of Hypertext Transfer Protocol (HTTP) response status codes.
 * The Internet Assigned Numbers Authority (IANA) maintains the official registry of HTTP status codes.
 * See <a href="https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml">Hypertext Transfer Protocol (HTTP) Status Code Registry</a>.
 */
public final class ResponseStatus {

    /**
     * 1xx informational status codes - request received, continuing process
     */
    public static class Info1xx {
        /**
         * 100 Continue.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-informational-1xx">HTTP Semantics</a>.
         */
        public static final Response.StatusType CONTINUE_100 = new ResponseStatusImpl(100, "Continue");
        /**
         * 101 Switching Protocols.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-101-switching-protocols">HTTP Semantics</a>.
         */
        public static final Response.StatusType SWITCHING_PROTOCOLS_101 = new ResponseStatusImpl(101, "Switching Protocols");
        /**
         * 102 Processing.
         * See <a href="https://www.rfc-editor.org/rfc/rfc2518#section-10.1">HTTP Extensions for Distributed Authoring -- WEBDAV</a>.
         */
        public static final Response.StatusType PROCESSING_102 = new ResponseStatusImpl(102, "Processing");
        /**
         * 103 Early Hints.
         * See <a href="https://www.rfc-editor.org/rfc/rfc2518#section-10.2">An HTTP Status Code for Indicating Hints</a>.
         */
        public static final Response.StatusType EARLY_HINTS_103 = new ResponseStatusImpl(103, "Early Hints");
    }

    /**
     * 2xx success status codes - the action was successfully received, understood, and accepted.
     */
    public static class Success2xx {
        /**
         * 200 OK.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-200-ok">HTTP Semantics</a>.
         */
        public static final Response.StatusType OK_200 = new ResponseStatusImpl(200, "OK");
        /**
         * 201 Created.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-201-created">HTTP Semantics</a>.
         */
        public static final Response.StatusType CREATED_201 = new ResponseStatusImpl(201, "Created");
        /**
         * 202 Accepted.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-202-accepted">HTTP Semantics</a>.
         */
        public static final Response.StatusType ACCEPTED_202 = new ResponseStatusImpl(202, "Accepted");
        /**
         * 203 Non-Authoritative Information.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-203-non-authoritative-infor">HTTP Semantics</a>.
         */
        public static final Response.StatusType NON_AUTHORITATIVE_INFORMATION_203
                = new ResponseStatusImpl(203, "Non-Authoritative Information");
        /**
         * 204 No Content.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-204-no-content">HTTP Semantics</a>.
         */
        public static final Response.StatusType NO_CONTENT_204 = new ResponseStatusImpl(204, "No Content");
        /**
         * 205 Reset Content.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-205-reset-content">HTTP Semantics</a>.
         */
        public static final Response.StatusType RESET_CONTENT_205 = new ResponseStatusImpl(205, "Reset Content");
        /**
         * 206 Partial Content.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-206-partial-content">HTTP Semantics</a>.
         */
        public static final Response.StatusType PARTIAL_CONTENT_206 = new ResponseStatusImpl(206, "Partial Content");
        /**
         * 207 Multi-Status.
         * See <a href="https://www.rfc-editor.org/rfc/rfc4918#section-10.7">HTTP Extensions for Web Distributed Authoring and Versioning  = new ResponseStatusImpl(WebDAV)</a>
         */
        public static final Response.StatusType MULTI_STATUS_207 = new ResponseStatusImpl(207, "Multi-Status");
        /**
         * 208 Already Reported.
         * See <a href="https://www.rfc-editor.org/rfc/rfc5842#section-7.1">Binding Extensions to Web Distributed Authoring and Versioning  = new ResponseStatusImpl(WebDAV)</a>
         */
        public static final Response.StatusType ALREADY_REPORTED_208 = new ResponseStatusImpl(208, "Already Reported");
        /**
         * 226 IM used.
         * See <a href="https://www.rfc-editor.org/rfc/rfc3229#section-10.4.1">Delta encoding in HTTP</a>
         */
        public static final Response.StatusType IM_USED_226 = new ResponseStatusImpl(226, "IM used");
    }

    /**
     * 3xx redirection status codes - further action must be taken in order to complete the request.
     */
    public static class Redirect3xx {
        /**
         * 300 Multiple Choices.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-300-multiple-choices">HTTP Semantics</a>.
         */
        public static final Response.StatusType MULTIPLE_CHOICES_300 = new ResponseStatusImpl(300, "Multiple Choices");
        /**
         * 301 Moved Permanently.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-301-moved-permanently">HTTP Semantics</a>.
         */
        public static final Response.StatusType MOVED_PERMANENTLY_301 = new ResponseStatusImpl(301, "Moved Permanently");
        /**
         * 302 Found.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-302-found">HTTP Semantics</a>.
         */
        public static final Response.StatusType FOUND_302 = new ResponseStatusImpl(302, "Found");
        /**
         * 303 See Other.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-303-see-other">HTTP Semantics</a>.
         */
        public static final Response.StatusType SEE_OTHER_303 = new ResponseStatusImpl(303, "See Other");
        /**
         * 304 Not Modified.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-304-not-modified">HTTP Semantics</a>.
         */
        public static final Response.StatusType NOT_MODIFIED_304 = new ResponseStatusImpl(304, "Not Modified");
        /**
         * 305 Use Proxy.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-305-use-proxy">HTTP Semantics</a>.
         */
        public static final Response.StatusType USE_PROXY_305 = new ResponseStatusImpl(305, "Use Proxy");
        /**
         * 307 Temporary Redirect.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-307-temporary-redirect">HTTP Semantics</a>.
         */
        public static final Response.StatusType TEMPORARY_REDIRECT_307 = new ResponseStatusImpl(307, "Temporary Redirect");
        /**
         * 308 Permanent Redirect.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-308-permanent-redirect">HTTP Semantics</a>.
         */
        public static final Response.StatusType PERMANENT_REDIRECT_308 = new ResponseStatusImpl(308, "Permanent Redirect");
    }

    /**
     * 4xx client error status codes - the request contains bad syntax or cannot be fulfilled.
     */
    public static class ClientError4xx {
        /**
         * 400 Bad Request.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-400-bad-request">HTTP Semantics</a>.
         */
        public static final Response.StatusType BAD_REQUEST_400 = new ResponseStatusImpl(400, "Bad Request");
        /**
         * 401 Unauthorized.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-401-unauthorized">HTTP Semantics</a>.
         */
        public static final Response.StatusType UNAUTHORIZED_401 = new ResponseStatusImpl(401, "Unauthorized");
        /**
         * 402 Payment Required.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-402-payment-required">HTTP Semantics</a>.
         */
        public static final Response.StatusType PAYMENT_REQUIRED_402 = new ResponseStatusImpl(402, "Payment Required");
        /**
         * 403 Forbidden.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-403-forbidden">HTTP Semantics</a>.
         */
        public static final Response.StatusType FORBIDDEN_403 = new ResponseStatusImpl(403, "Forbidden");
        /**
         * 404 Not Found.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-404-not-found">HTTP Semantics</a>.
         */
        public static final Response.StatusType NOT_FOUND_404 = new ResponseStatusImpl(404, "Not Found");
        /**
         * 405 Method Not Allowed.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-405-method-not-allowed">HTTP Semantics</a>.
         */
        public static final Response.StatusType METHOD_NOT_ALLOWED_405 = new ResponseStatusImpl(405, "Method Not Allowed");
        /**
         * 406 Not Acceptable.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-406-not-acceptable">HTTP Semantics</a>.
         */
        public static final Response.StatusType NOT_ACCEPTABLE_406 = new ResponseStatusImpl(406, "Not Acceptable");
        /**
         * 407 Proxy Authentication Required.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-407-proxy-authentication-re">HTTP Semantics</a>.
         */
        public static final Response.StatusType PROXY_AUTHENTICATION_REQUIRED_407
                = new ResponseStatusImpl(407, "Proxy Authentication Required");
        /**
         * 408 Request Timeout.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-408-request-timeout">HTTP Semantics</a>.
         */
        public static final Response.StatusType REQUEST_TIMEOUT_408 = new ResponseStatusImpl(408, "Request Timeout");
        /**
         * 409 Conflict.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-409-conflict">HTTP Semantics</a>.
         */
        public static final Response.StatusType CONFLICT_409 = new ResponseStatusImpl(409, "Conflict");
        /**
         * 410 Gone.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-410-gone">HTTP Semantics</a>.
         */
        public static final Response.StatusType GONE_410 = new ResponseStatusImpl(410, "Gone");
        /**
         * 411 Length Required.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-411-length-required">HTTP Semantics</a>.
         */
        public static final Response.StatusType LENGTH_REQUIRED_411 = new ResponseStatusImpl(411, "Length Required");
        /**
         * 412 Precondition Failed.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-412-precondition-failed">HTTP Semantics</a>.
         */
        public static final Response.StatusType PRECONDITION_FAILED_412 = new ResponseStatusImpl(412, "Precondition Failed");
        /**
         * 413 Request Entity Too Large.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-413-content-too-large">HTTP Semantics</a>.
         */
        public static final Response.StatusType REQUEST_ENTITY_TOO_LARGE_413
                = new ResponseStatusImpl(413, "Request Entity Too Large");
        /**
         * 414 Request-URI Too Long.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-414-uri-too-long">HTTP Semantics</a>.
         */
        public static final Response.StatusType REQUEST_URI_TOO_LONG_414 = new ResponseStatusImpl(414, "Request-URI Too Long");
        /**
         * 415 Unsupported Media Type.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-415-unsupported-media-type">HTTP Semantics</a>.
         */
        public static final Response.StatusType UNSUPPORTED_MEDIA_TYPE_415
                = new ResponseStatusImpl(415, "Unsupported Media Type");
        /**
         * 416 Requested Range Not Satisfiable.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-416-range-not-satisfiable">HTTP Semantics</a>.
         */
        public static final Response.StatusType REQUESTED_RANGE_NOT_SATISFIABLE_416
                = new ResponseStatusImpl(416, "Requested Range Not Satisfiable");
        /**
         * 417 Expectation Failed.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-417-expectation-failed">HTTP Semantics</a>.
         */
        public static final Response.StatusType EXPECTATION_FAILED_417 = new ResponseStatusImpl(417, "Expectation Failed");
        /**
         * 418 I'm a teapot.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-418-unused">HTTP Semantics</a>
         * and <a href="https://www.rfc-editor.org/rfc/rfc7168#page-5">Hyper Text Coffee Pot Control Protocol</a>
         */
        public static final Response.StatusType I_AM_A_TEAPOT_418 = new ResponseStatusImpl(418, "I'm a teapot");
        /**
         * 421 Misdirected Request.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-421-misdirected-request">HTTP Semantics</a>.
         */
        public static final Response.StatusType MISDIRECTED_REQUEST_421 = new ResponseStatusImpl(421, "Misdirected Request");
        /**
         * 422 Unprocessable Content.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-422-unprocessable-content">HTTP Semantics</a>.
         */
        public static final Response.StatusType UNPROCESSABLE_CONTENT_422 = new ResponseStatusImpl(422, "Unprocessable Content");
        /**
         * 423 Locked.
         * See <a href="https://www.rfc-editor.org/rfc/rfc4918#section-11.3">HTTP Extensions for Web Distributed Authoring and Versioning  = new ResponseStatusImpl(WebDAV)</a>
         */
        public static final Response.StatusType LOCKED_423 = new ResponseStatusImpl(423, "Locked");
        /**
         * 424 Failed Dependency.
         * See <a href="https://www.rfc-editor.org/rfc/rfc4918#section-11.4">HTTP Extensions for Web Distributed Authoring and Versioning  = new ResponseStatusImpl(WebDAV)</a>
         */
        public static final Response.StatusType FAILED_DEPENDENCY_424 = new ResponseStatusImpl(424, "Failed Dependency");
        /**
         * 425 Too Early.
         * See <a href="https://www.rfc-editor.org/rfc/rfc8470#section-5.2">Using Early Data in HTTP</a>.
         */
        public static final Response.StatusType TOO_EARLY_425 = new ResponseStatusImpl(425, "Too Early");
        /**
         * 426 Upgrade Required.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-426-upgrade-required">HTTP Semantics</a>.
         */
        public static final Response.StatusType UPGRADE_REQUIRED_426 = new ResponseStatusImpl(426, "Upgrade Required");
        /**
         * 428 Precondition Required.
         * See <a href="https://www.rfc-editor.org/rfc/rfc6585.html#page-2">Additional HTTP Status Codes</a>.
         */
        public static final Response.StatusType PRECONDITION_REQUIRED_428 = new ResponseStatusImpl(428, "Precondition Required");
        /**
         * 429 Too Many Requests.
         * See <a href="https://www.rfc-editor.org/rfc/rfc6585.html#page-3">Additional HTTP Status Codes</a>.
         */
        public static final Response.StatusType TOO_MANY_REQUESTS_429 = new ResponseStatusImpl(429, "Too Many Requests");
        /**
         * 431 Request Header Fields Too Large.
         * See <a href="https://www.rfc-editor.org/rfc/rfc6585.html#page-4">Additional HTTP Status Codes</a>.
         */
        public static final Response.StatusType REQUEST_HEADER_FIELDS_TOO_LARGE_431
                = new ResponseStatusImpl(431, "Request Header Fields Too Large");
        /**
         * 451 Unavailable For Legal Reasons.
         * See <a href="https://www.rfc-editor.org/rfc/rfc7725#page-2">An HTTP Status Code to Report Legal Obstacles</a>.
         */
        public static final Response.StatusType UNAVAILABLE_FOR_LEGAL_REASONS_451
                = new ResponseStatusImpl(451, "Unavailable For Legal Reasons");
    }

    /**
     * 5xx server error status codes - the server failed to fulfill an apparently valid request.
     */
    public static class ServerError5xx {
        /**
         * 500 Internal Server Error.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-server-error-5xx">HTTP Semantics</a>.
         */
        public static final Response.StatusType INTERNAL_SERVER_ERROR_500 = new ResponseStatusImpl(500, "Internal Server Error");
        /**
         * 501 Not Implemented.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-501-not-implemented">HTTP Semantics</a>.
         */
        public static final Response.StatusType NOT_IMPLEMENTED_501 = new ResponseStatusImpl(501, "Not Implemented");
        /**
         * 502 Bad Gateway.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-502-bad-gateway">HTTP Semantics</a>.
         */
        public static final Response.StatusType BAD_GATEWAY_502 = new ResponseStatusImpl(502, "Bad Gateway");
        /**
         * 503 Service Unavailable.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-503-service-unavailable">HTTP Semantics</a>.
         */
        public static final Response.StatusType SERVICE_UNAVAILABLE_503 = new ResponseStatusImpl(503, "Service Unavailable");
        /**
         * 504 Gateway Timeout.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-504-gateway-timeout">HTTP Semantics</a>.
         */
        public static final Response.StatusType GATEWAY_TIMEOUT_504 = new ResponseStatusImpl(504, "Gateway Timeout");
        /**
         * 505 HTTP Version Not Supported.
         * See <a href="https://www.rfc-editor.org/rfc/rfc9110#name-505-http-version-not-suppor">HTTP Semantics</a>.
         */
        public static final Response.StatusType HTTP_VERSION_NOT_SUPPORTED_505
                = new ResponseStatusImpl(505, "HTTP Version Not Supported");
        /**
         * 506 Variant Also Negotiates.
         * See <a href="https://www.rfc-editor.org/rfc/rfc2295#section-8.1">Transparent Content Negotiation in HTTP</a>.
         */
        public static final Response.StatusType VARIANT_ALSO_NEGOTIATES_506
                = new ResponseStatusImpl(506, "Variant Also Negotiates");
        /**
         * 507 Insufficient Storage.
         * See <a href="https://www.rfc-editor.org/rfc/rfc4918#section-11.5">HTTP Extensions for Web Distributed Authoring and Versioning  = new ResponseStatusImpl(WebDAV)</a>
         */
        public static final Response.StatusType INSUFFICIENT_STORAGE_507 = new ResponseStatusImpl(507, "Insufficient Storage");
        /**
         * 508 Loop Detected.
         * See <a href="https://www.rfc-editor.org/rfc/rfc5842#page-34">Binding Extensions to Web Distributed Authoring and Versioning  = new ResponseStatusImpl(WebDAV)</a>
         */
        public static final Response.StatusType LOOP_DETECTED_508 = new ResponseStatusImpl(508, "Loop Detected");
        /**
         * 510 Not Extended.
         * See <a href="https://www.rfc-editor.org/rfc/rfc2774#section-7">An HTTP Extension Framework</a>.
         */
        public static final Response.StatusType NOT_EXTENDED_510 = new ResponseStatusImpl(510, "Not Extended");
        /**
         * 511 Network Authentication Required.
         * See <a href="https://www.rfc-editor.org/rfc/rfc6585.html#page-4">Additional HTTP Status Codes</a>.
         */
        public static final Response.StatusType NETWORK_AUTHENTICATION_REQUIRED_511
                = new ResponseStatusImpl(511, "Network Authentication Required");
    }

    private static class ResponseStatusImpl implements Response.StatusType {
        private final int statusCode;
        private final String reasonPhrase;
        private final Response.Status.Family family;

        private ResponseStatusImpl(int statusCode, String reasonPhrase) {
            this.statusCode = statusCode;
            this.reasonPhrase = reasonPhrase;
            this.family = Response.Status.Family.familyOf(statusCode);
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public Response.Status.Family getFamily() {
            return family;
        }

        @Override
        public String getReasonPhrase() {
            return reasonPhrase;
        }
    }
}
