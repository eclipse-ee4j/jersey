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

package org.glassfish.jersey.message.internal;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.uri.UriTemplate;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;

/**
 * Jersey implementation of {@link javax.ws.rs.core.Link JAX-RS Link} contract.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class JerseyLink extends javax.ws.rs.core.Link {
    /**
     * Underlying builder for link's URI.
     */
    private final URI uri;
    /**
     * A map for all the link parameters such as "rel", "type", etc.
     */
    private final Map<String, String> params;

    /**
     * Jersey implementation of {@link javax.ws.rs.core.Link.Builder JAX-RS Link.Builder} contract.
     */
    public static class Builder implements javax.ws.rs.core.Link.Builder {
        /**
         * Underlying builder for link's URI.
         */
        private UriBuilder uriBuilder = new JerseyUriBuilder();
        /**
         * Base URI for resolution of a link URI (if relative).
         */
        private URI baseUri = null;
        /**
         * A map for all the link parameters such as "rel", "type", etc.
         */
        private Map<String, String> params = new HashMap<String, String>();

        @Override
        public Builder link(javax.ws.rs.core.Link link) {
            uriBuilder.uri(link.getUri());
            params.clear();
            params.putAll(link.getParams());
            return this;
        }

        @Override
        public Builder link(String link) {
            LinkProvider.initBuilder(this, link);
            return this;
        }

        @Override
        public Builder uri(URI uri) {
            this.uriBuilder = UriBuilder.fromUri(uri);
            return this;
        }

        @Override
        public Builder uri(String uri) {
            this.uriBuilder = UriBuilder.fromUri(uri);
            return this;
        }

        @Override
        public Builder uriBuilder(UriBuilder uriBuilder) {
            this.uriBuilder = UriBuilder.fromUri(uriBuilder.toTemplate());
            return this;
        }

        @Override
        public Link.Builder baseUri(URI uri) {
            this.baseUri = uri;
            return this;
        }

        @Override
        public Link.Builder baseUri(String uri) {
            this.baseUri = URI.create(uri);
            return this;
        }

        @Override
        public Builder rel(String rel) {
            final String rels = params.get(REL);
            param(REL, rels == null ? rel : rels + " " + rel);
            return this;
        }

        @Override
        public Builder title(String title) {
            param(TITLE, title);
            return this;
        }

        @Override
        public Builder type(String type) {
            param(TYPE, type);
            return this;
        }

        @Override
        public Builder param(String name, String value) {
            if (name == null || value == null) {
                throw new IllegalArgumentException("Link parameter name or value is null");
            }
            params.put(name, value);
            return this;
        }

        @Override
        public JerseyLink build(Object... values) {
            final URI linkUri = resolveLinkUri(values);
            return new JerseyLink(linkUri, Collections.unmodifiableMap(new HashMap<String, String>(params)));
        }

        @Override
        public Link buildRelativized(URI uri, Object... values) {
            final URI linkUri = UriTemplate.relativize(uri, resolveLinkUri(values));
            return new JerseyLink(linkUri, Collections.unmodifiableMap(new HashMap<String, String>(params)));
        }

        private URI resolveLinkUri(Object[] values) {
            final URI linkUri = uriBuilder.build(values);
            if (baseUri == null || linkUri.isAbsolute()) {
                return UriTemplate.normalize(linkUri);
            }
            return UriTemplate.resolve(baseUri, linkUri);
        }
    }

    private JerseyLink(URI uri, Map<String, String> params) {
        this.uri = uri;
        this.params = params;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public UriBuilder getUriBuilder() {
        return new JerseyUriBuilder().uri(uri);
    }

    @Override
    public String getRel() {
        return params.get(REL);
    }

    @Override
    public List<String> getRels() {
        final String rels = params.get(REL);
        return rels == null ? Collections.<String>emptyList() : Arrays.asList(rels.split(" +"));
    }

    @Override
    public String getTitle() {
        return params.get(TITLE);
    }

    @Override
    public String getType() {
        return params.get(TYPE);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return LinkProvider.stringfy(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Link) {
            final Link otherLink = (Link) other;
            return uri.equals(otherLink.getUri()) && params.equals(otherLink.getParams());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.uri != null ? this.uri.hashCode() : 0);
        hash = 89 * hash + (this.params != null ? this.params.hashCode() : 0);
        return hash;
    }
}
