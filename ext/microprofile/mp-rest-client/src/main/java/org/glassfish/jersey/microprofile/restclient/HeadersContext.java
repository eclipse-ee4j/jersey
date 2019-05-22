/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.util.Optional;
import java.util.function.Supplier;

import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author David Kral
 */
final class HeadersContext {

    /**
     * Headers context thread local, used by internal implementations of header filters.
     */
    private static final ThreadLocal<HeadersContext> HEADERS_CONTEXT = new ThreadLocal<>();

    private final MultivaluedMap<String, String> inboundHeaders;

    /**
     * The instance associated with the current thread.
     * @return context for current thread or {@code empty} if none associated
     */
    static Optional<HeadersContext> get() {
        return Optional.ofNullable(HEADERS_CONTEXT.get());
    }

    /**
     * Computes the instance and associates it with current thread if none
     * associated, or returns the instance already associated.
     *
     * @param contextSupplier supplier for header context to be associated with the thread if none is
     * @return an instance associated with the current context, either from other provider, or from contextSupplier
     */
    static HeadersContext compute(Supplier<HeadersContext> contextSupplier) {
        HeadersContext headersContext = HEADERS_CONTEXT.get();
        if (null == headersContext) {
            set(contextSupplier.get());
        }

        return get().orElseThrow(() -> new IllegalStateException("Computed result was null"));
    }

    /**
     * Set the header context to be associated with current thread.
     *
     * @param context context to associate
     */
    static void set(HeadersContext context) {
        HEADERS_CONTEXT.set(context);
    }

    /**
     * Remove the header context associated with current thread.
     */
    static void remove() {
        HEADERS_CONTEXT.remove();
    }

    /**
     * Create a new header context with client tracing enabled.
     *
     * @param inboundHeaders inbound header to be used for context propagation
     * @return a new header context (not associated with current thread)
     * @see #set(HeadersContext)
     */
    static HeadersContext create(MultivaluedMap<String, String> inboundHeaders) {
        return new HeadersContext(inboundHeaders);
    }

    private HeadersContext(MultivaluedMap<String, String> inboundHeaders) {
        this.inboundHeaders = inboundHeaders;
    }

    /**
     * Map of headers that were received by server for an inbound call,
     * may be used to propagate additional headers fro outbound request.
     *
     * @return map of inbound headers
     */
    MultivaluedMap<String, String> inboundHeaders() {
        return inboundHeaders;
    }


}
