/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import javax.annotation.Priority;

/**
 * Standard contract for plugging in content encoding support. Provides a standard way of implementing encoding
 * {@link WriterInterceptor} and decoding {@link ReaderInterceptor}. Implementing this class ensures the encoding
 * supported by the implementation will be considered during the content negotiation phase when deciding which encoding
 * should be used based on the accepted encodings (and the associated quality parameters) in the request headers.
 *
 * @author Martin Matula
 */
@Priority(Priorities.ENTITY_CODER)
@Contract
public abstract class ContentEncoder implements ReaderInterceptor, WriterInterceptor {
    private final Set<String> supportedEncodings;

    /**
     * Initializes this encoder implementation with the list of supported content encodings.
     *
     * @param supportedEncodings Values of Content-Encoding header supported by this encoding provider.
     */
    protected ContentEncoder(String... supportedEncodings) {
        if (supportedEncodings.length == 0) {
            throw new IllegalArgumentException();
        }
        this.supportedEncodings = Collections.unmodifiableSet(Arrays.stream(supportedEncodings).collect(Collectors.toSet()));
    }

    /**
     * Returns values of Content-Encoding header supported by this encoder.
     * @return Set of supported Content-Encoding values.
     */
    public final Set<String> getSupportedEncodings() {
        return supportedEncodings;
    }

    /**
     * Implementations of this method should take the encoded stream, wrap it and return a stream that can be used
     * to read the decoded entity.
     *
     *
     * @param contentEncoding Encoding to be used to decode the stream - guaranteed to be one of the supported encoding
     *                        values.
     * @param encodedStream Encoded input stream.
     * @return Decoded entity stream.
     * @throws java.io.IOException if an IO error arises.
     */
    public abstract InputStream decode(String contentEncoding, InputStream encodedStream) throws IOException;

    /**
     * Implementations of this method should take the entity stream, wrap it and return a stream that is encoded
     * using the specified encoding.
     *
     *
     * @param contentEncoding Encoding to be used to encode the entity - guaranteed to be one of the supported encoding
     *                        values.
     * @param entityStream Entity stream to be encoded.
     * @return Encoded stream.
     * @throws java.io.IOException if an IO error arises.
     */
    public abstract OutputStream encode(String contentEncoding, OutputStream entityStream) throws IOException;

    @Override
    public final Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        String contentEncoding = context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if (contentEncoding != null && getSupportedEncodings().contains(contentEncoding)) {
            context.setInputStream(decode(contentEncoding, context.getInputStream()));
        }
        return context.proceed();
    }

    @Override
    public final void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        // must remove Content-Length header since the encoded message will have a different length

        String contentEncoding = (String) context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if (contentEncoding != null && getSupportedEncodings().contains(contentEncoding)) {
            context.setOutputStream(encode(contentEncoding, context.getOutputStream()));
        }
        context.proceed();
    }
}
