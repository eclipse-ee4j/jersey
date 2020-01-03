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

package org.glassfish.jersey.client;

import org.glassfish.jersey.client.spi.InvocationBuilderListener;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.model.internal.RankedComparator;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Client request processing stage. During a request creation, when the {@link Invocation.Builder}
 * would be created, this class is utilized.
 */
/* package */ class InvocationBuilderListenerStage {
    final Iterator<InvocationBuilderListener> invocationBuilderListenerIterator;

    /* package */ InvocationBuilderListenerStage(InjectionManager injectionManager) {
        final RankedComparator<InvocationBuilderListener> comparator =
                new RankedComparator<>(RankedComparator.Order.ASCENDING);
        invocationBuilderListenerIterator = Providers
                .getAllProviders(injectionManager, InvocationBuilderListener.class, comparator).iterator();
    }

    /* package */ void invokeListener(JerseyInvocation.Builder builder) {
        while (invocationBuilderListenerIterator.hasNext()) {
            invocationBuilderListenerIterator.next().onNewBuilder(new InvocationBuilderContextImpl(builder));
        }
    }

    private static class InvocationBuilderContextImpl implements InvocationBuilderListener.InvocationBuilderContext {
        private final JerseyInvocation.Builder builder;

        private InvocationBuilderContextImpl(JerseyInvocation.Builder builder) {
            this.builder = builder;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext accept(String... mediaTypes) {
            builder.accept(mediaTypes);
            return this;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext accept(MediaType... mediaTypes) {
            builder.accept(mediaTypes);
            return this;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext acceptLanguage(Locale... locales) {
            builder.acceptLanguage(locales);
            return this;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext acceptLanguage(String... locales) {
            builder.acceptLanguage(locales);
            return this;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext acceptEncoding(String... encodings) {
            builder.acceptEncoding(encodings);
            return this;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext cookie(Cookie cookie) {
            builder.cookie(cookie);
            return this;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext cookie(String name, String value) {
            builder.cookie(name, value);
            return this;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext cacheControl(CacheControl cacheControl) {
            builder.cacheControl(cacheControl);
            return this;
        }

        @Override
        public List<String> getAccepted() {
            return getHeader(HttpHeaders.ACCEPT);
        }

        @Override
        public List<String> getAcceptedLanguages() {
            return getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        }

        @Override
        public List<CacheControl> getCacheControls() {
            return (List<CacheControl>) (List<?>) builder.request().getHeaders().get(HttpHeaders.CACHE_CONTROL);
        }

        @Override
        public Configuration getConfiguration() {
            return builder.request().getConfiguration();
        }

        @Override
        public Map<String, Cookie> getCookies() {
            return builder.request().getCookies();
        }

        @Override
        public List<String> getEncodings() {
            return getHeader(HttpHeaders.ACCEPT_ENCODING);
        }

        @Override
        public List<String> getHeader(String name) {
            return builder.request().getRequestHeader(name);
        }

        @Override
        public MultivaluedMap<String, Object> getHeaders() {
            return builder.request().getHeaders();
        }

        @Override
        public Object getProperty(String name) {
            return builder.request().getProperty(name);
        }

        @Override
        public Collection<String> getPropertyNames() {
            return builder.request().getPropertyNames();
        }

        @Override
        public URI getUri() {
            return builder.request().getUri();
        }


        @Override
        public InvocationBuilderListener.InvocationBuilderContext header(String name, Object value) {
            builder.header(name, value);
            return this;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext headers(MultivaluedMap<String, Object> headers) {
            builder.headers(headers);
            return this;
        }

        @Override
        public InvocationBuilderListener.InvocationBuilderContext property(String name, Object value) {
            builder.property(name, value);
            return this;
        }

        @Override
        public void removeProperty(String name) {
            builder.request().removeProperty(name);
        }
    }
}

