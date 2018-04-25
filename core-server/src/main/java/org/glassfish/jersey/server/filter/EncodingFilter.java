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

package org.glassfish.jersey.server.filter;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;

import javax.annotation.Priority;
import javax.inject.Inject;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.message.internal.HttpHeaderReader;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.spi.ContentEncoder;

/**
 * Container filter that supports encoding-based content negotiation. The filter examines what
 * content encodings are supported by the container (by looking up all the
 * {@link org.glassfish.jersey.spi.ContentEncoder encoders}) and decides what encoding should be chosen
 * based on the encodings listed in the Accept-Encoding request header and their associated quality values.
 * If none of the acceptable encodings is supported and identity encoding is explicitly forbidden by the client,
 * the filter generates {@link javax.ws.rs.core.Response.Status#NOT_ACCEPTABLE} response.
 * <p>
 *     The filter also ensures Accept-Encoding is added to the Vary header, for proper interaction with web caches.
 * </p>
 *
 * @author Martin Matula
 */
@Priority(Priorities.HEADER_DECORATOR)
public final class EncodingFilter implements ContainerResponseFilter {
    // name for the identity encoding
    private static final String IDENTITY_ENCODING = "identity";

    @Inject
    private InjectionManager injectionManager;
    // sorted set to keep the order same for different invocations of the app
    private volatile SortedSet<String> supportedEncodings = null;

    /**
     * Enables this filter along with the provided {@link org.glassfish.jersey.spi.ContentEncoder encoders}
     * for the supplied {@link ResourceConfig}.
     *
     * @param rc Resource config this filter should be enabled for.
     * @param encoders content encoders.
     */
    @SafeVarargs
    public static void enableFor(ResourceConfig rc, Class<? extends ContentEncoder>... encoders) {
        rc.registerClasses(encoders).registerClasses(EncodingFilter.class);
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        if (!response.hasEntity()) {
            return;
        }

        // add Accept-Encoding to Vary header
        List<String> varyHeader = response.getStringHeaders().get(HttpHeaders.VARY);
        if (varyHeader == null || !varyHeader.contains(HttpHeaders.ACCEPT_ENCODING)) {
            response.getHeaders().add(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
        }

        // if Content-Encoding is already set, don't do anything
        if (response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING) != null) {
            return;
        }

        // retrieve the list of accepted encodings
        List<String> acceptEncoding = request.getHeaders().get(HttpHeaders.ACCEPT_ENCODING);

        // if empty, don't do anything
        if (acceptEncoding == null || acceptEncoding.isEmpty()) {
            return;
        }

        // convert encodings from String to Encoding objects
        List<ContentEncoding> encodings = new ArrayList<>();
        for (String input : acceptEncoding) {
            String[] tokens = input.split(",");
            for (String token : tokens) {
                try {
                    ContentEncoding encoding = ContentEncoding.fromString(token);
                    encodings.add(encoding);
                } catch (ParseException e) {
                    // ignore the encoding that could not parse
                    // but log the exception
                    Logger.getLogger(EncodingFilter.class.getName()).log(Level.WARNING, e.getLocalizedMessage(), e);
                }
            }
        }
        // sort based on quality parameter
        Collections.sort(encodings);
        // make sure IDENTITY_ENCODING is at the end (since it accepted if not explicitly forbidden
        // in the Accept-Content header by assigning q=0
        encodings.add(new ContentEncoding(IDENTITY_ENCODING, -1));

        // get a copy of supported encoding (we'll be modifying this set, hence the copy)
        SortedSet<String> acceptedEncodings = new TreeSet<>(getSupportedEncodings());

        // indicates that we can pick any of the encodings that remained in the acceptedEncodings set
        boolean anyRemaining = false;
        // final resulting value of the Content-Encoding header to be set
        String contentEncoding = null;

        // iterate through the accepted encodings, starting with the highest quality one
        for (ContentEncoding encoding : encodings) {
            if (encoding.q == 0) {
                // ok, we are down at 0 quality
                if ("*".equals(encoding.name)) {
                    // no other encoding is acceptable
                    break;
                }
                // all the 0 quality encodings need to be removed from the accepted ones (these are explicitly
                // forbidden by the client)
                acceptedEncodings.remove(encoding.name);
            } else {
                if ("*".equals(encoding.name)) {
                    // any remaining encoding (after filtering out q=0) will be acceptable
                    anyRemaining = true;
                } else {
                    if (acceptedEncodings.contains(encoding.name)) {
                        // found an acceptable one -> we are done
                        contentEncoding = encoding.name;
                        break;
                    }
                }
            }
        }

        if (contentEncoding == null) {
            // haven't found any explicit acceptable encoding, let's see if we can just pick any of the remaining ones
            // (if there are any left)
            if (anyRemaining && !acceptedEncodings.isEmpty()) {
                contentEncoding = acceptedEncodings.first();
            } else {
                // no acceptable encoding can be sent -> return NOT ACCEPTABLE status code back to the client
                throw new NotAcceptableException();
            }
        }

        // finally set the header - but no need to set for identity encoding
        if (!IDENTITY_ENCODING.equals(contentEncoding)) {
            response.getHeaders().putSingle(HttpHeaders.CONTENT_ENCODING, contentEncoding);
        }
    }

    // representation of a single Content-Encoding header value
    private static class ContentEncoding implements Comparable<ContentEncoding> {
        public final String name;
        public final int q;

        public ContentEncoding(String encoding, int q) {
            this.name = encoding;
            this.q = q;
        }

        public static ContentEncoding fromString(String input) throws ParseException {
            HttpHeaderReader reader = HttpHeaderReader.newInstance(input);

            // Skip any white space
            reader.hasNext();

            return new ContentEncoding(reader.nextToken().toString(), HttpHeaderReader.readQualityFactorParameter(reader));

        }

        @Override
        public int hashCode() {
            return 41 * name.hashCode() + q;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj != null && (obj instanceof ContentEncoding) && name.equals(((ContentEncoding) obj).name)
                    && q == ((ContentEncoding) obj).q;
        }

        @Override
        public int compareTo(ContentEncoding o) {
            // higher q goes first (i.e. descending order)
            return Integer.compare(o.q, q);
        }
    }

    /**
     * Returns a (lexically) sorted set of supported encodings.
     * @return sorted set of supported encodings.
     */
    SortedSet<String> getSupportedEncodings() {
        // no need for synchronization - in case of a race condition, the property
        // may be set twice, but it does not break anything
        if (supportedEncodings == null) {
            SortedSet<String> se = new TreeSet<>();
            List<ContentEncoder> encoders = injectionManager.getAllInstances(ContentEncoder.class);
            for (ContentEncoder encoder : encoders) {
                se.addAll(encoder.getSupportedEncodings());
            }
            se.add(IDENTITY_ENCODING);
            supportedEncodings = se;
        }
        return supportedEncodings;
    }
}
