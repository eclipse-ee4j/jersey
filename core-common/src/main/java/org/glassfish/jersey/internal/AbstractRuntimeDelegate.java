/*
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal;

import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.innate.spi.EntityPartBuilderProvider;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.message.internal.JerseyLink;
import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.message.internal.VariantListBuilder;
import org.glassfish.jersey.spi.HeaderDelegateProvider;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;

/**
 * An abstract implementation of {@link RuntimeDelegate} that
 * provides support common to the client and server.
 *
 * @author Paul Sandoz
 */
public abstract class AbstractRuntimeDelegate extends RuntimeDelegate {

    private final Set<HeaderDelegateProvider> hps;
    private final Map<Class<?>, HeaderDelegate<?>> map;
    private LazyValue<EntityPartBuilderProvider> entityPartBuilderProvider = Values.lazy(
            (Value<EntityPartBuilderProvider>) () -> findEntityPartBuilderProvider());

    /**
     * Initialization constructor. The injection manager will be shut down.
     *
     * @param hps all {@link HeaderDelegateProvider} instances registered internally.
     */
    protected AbstractRuntimeDelegate(Set<HeaderDelegateProvider> hps) {
        this.hps = hps;

        /*
         * Construct a map for quick look up of known header classes
         */
        map = new WeakHashMap<>();
        map.put(EntityTag.class, _createHeaderDelegate(EntityTag.class));
        map.put(MediaType.class, _createHeaderDelegate(MediaType.class));
        map.put(CacheControl.class, _createHeaderDelegate(CacheControl.class));
        map.put(NewCookie.class, _createHeaderDelegate(NewCookie.class));
        map.put(Cookie.class, _createHeaderDelegate(Cookie.class));
        map.put(URI.class, _createHeaderDelegate(URI.class));
        map.put(Date.class, _createHeaderDelegate(Date.class));
        map.put(String.class, _createHeaderDelegate(String.class));
    }

    @Override
    public jakarta.ws.rs.core.Variant.VariantListBuilder createVariantListBuilder() {
        return new VariantListBuilder();
    }

    @Override
    public ResponseBuilder createResponseBuilder() {
        return new OutboundJaxrsResponse.Builder(new OutboundMessageContext((Configuration) null));
    }

    @Override
    public UriBuilder createUriBuilder() {
        return new JerseyUriBuilder();
    }

    @Override
    public Link.Builder createLinkBuilder() {
        return new JerseyLink.Builder();
    }

    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(final Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type parameter cannot be null");
        }

        @SuppressWarnings("unchecked") final HeaderDelegate<T> delegate = (HeaderDelegate<T>) map.get(type);
        if (delegate != null) {
            return delegate;
        }

        return _createHeaderDelegate(type);
    }

    @SuppressWarnings("unchecked")
    private <T> HeaderDelegate<T> _createHeaderDelegate(final Class<T> type) {
        for (final HeaderDelegateProvider hp : hps) {
            if (hp.supports(type)) {
                return hp;
            }
        }

        return null;
    }

    @Override
    public EntityPart.Builder createEntityPartBuilder(String partName) throws IllegalArgumentException {
        return entityPartBuilderProvider.get().withName(partName);
    }

    /**
     * Obtain a {@code RuntimeDelegate} instance using the method described in {@link #getInstance}.
     *
     * @return an instance of {@code RuntimeDelegate}.
     */
    private static EntityPartBuilderProvider findEntityPartBuilderProvider() {
        for (final EntityPartBuilderProvider entityPartBuilder : ServiceFinder.find(EntityPartBuilderProvider.class)) {
            if (entityPartBuilder != null) {
                return entityPartBuilder;
            }
        }

        throw new IllegalArgumentException(LocalizationMessages.NO_ENTITYPART_BUILDER_FOUND());
    }
}
