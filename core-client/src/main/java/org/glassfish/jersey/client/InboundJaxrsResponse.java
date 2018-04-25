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

package org.glassfish.jersey.client;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.util.Producer;
import org.glassfish.jersey.process.internal.RequestContext;
import org.glassfish.jersey.process.internal.RequestScope;

/**
 * Implementation of an inbound client-side JAX-RS {@link Response} message.
 * <p>
 * This response delegates method calls to the underlying
 * {@link org.glassfish.jersey.client.ClientResponse client response context} and
 * ensures that all request-scoped method invocations are run in the proper request scope.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
class InboundJaxrsResponse extends Response {

    private final ClientResponse context;
    private final RequestScope scope;
    private final RequestContext requestContext;

    /**
     * Create new scoped client response.
     *
     * @param context jersey client response context.
     * @param scope   request scope instance.
     */
    public InboundJaxrsResponse(final ClientResponse context, final RequestScope scope) {
        this.context = context;
        this.scope = scope;
        if (this.scope != null) {
            this.requestContext = scope.referenceCurrent();
        } else {
            this.requestContext = null;
        }
    }

    @Override
    public int getStatus() {
        return context.getStatus();
    }

    @Override
    public StatusType getStatusInfo() {
        return context.getStatusInfo();
    }

    @Override
    public Object getEntity() throws IllegalStateException {
        return context.getEntity();
    }

    @Override
    public <T> T readEntity(final Class<T> entityType) throws ProcessingException, IllegalStateException {
        return runInScopeIfPossible(new Producer<T>() {
            @Override
            public T call() {
                return context.readEntity(entityType);
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readEntity(final GenericType<T> entityType) throws ProcessingException, IllegalStateException {
        return runInScopeIfPossible(new Producer<T>() {
            @Override
            public T call() {
                return context.readEntity(entityType);
            }
        });
    }

    @Override
    public <T> T readEntity(final Class<T> entityType, final Annotation[] annotations)
            throws ProcessingException, IllegalStateException {
        return runInScopeIfPossible(new Producer<T>() {
            @Override
            public T call() {
                return context.readEntity(entityType, annotations);
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readEntity(final GenericType<T> entityType, final Annotation[] annotations)
            throws ProcessingException, IllegalStateException {
        return runInScopeIfPossible(new Producer<T>() {
            @Override
            public T call() {
                return context.readEntity(entityType, annotations);
            }
        });
    }

    @Override
    public boolean hasEntity() {
        return context.hasEntity();
    }

    @Override
    public boolean bufferEntity() throws ProcessingException {
        return context.bufferEntity();
    }

    @Override
    public void close() throws ProcessingException {
        try {
            context.close();
        } finally {
            if (requestContext != null) {
                requestContext.release();
            }
        }
    }

    @Override
    public String getHeaderString(String name) {
        return context.getHeaderString(name);
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        return context.getHeaders();
    }

    @Override
    public MediaType getMediaType() {
        return context.getMediaType();
    }

    @Override
    public Locale getLanguage() {
        return context.getLanguage();
    }

    @Override
    public int getLength() {
        return context.getLength();
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        return context.getResponseCookies();
    }

    @Override
    public EntityTag getEntityTag() {
        return context.getEntityTag();
    }

    @Override
    public Date getDate() {
        return context.getDate();
    }

    @Override
    public Date getLastModified() {
        return context.getLastModified();
    }

    @Override
    public Set<String> getAllowedMethods() {
        return context.getAllowedMethods();
    }

    @Override
    public URI getLocation() {
        return context.getLocation();
    }

    @Override
    public Set<Link> getLinks() {
        return context.getLinks();
    }

    @Override
    public boolean hasLink(String relation) {
        return context.hasLink(relation);
    }

    @Override
    public Link getLink(String relation) {
        return context.getLink(relation);
    }

    @Override
    public Link.Builder getLinkBuilder(String relation) {
        return context.getLinkBuilder(relation);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MultivaluedMap<String, Object> getMetadata() {
        final MultivaluedMap<String, ?> headers = context.getHeaders();
        return (MultivaluedMap<String, Object>) headers;
    }

    @Override
    public String toString() {
        return "InboundJaxrsResponse{" + "context=" + context + "}";
    }

    private <T> T runInScopeIfPossible(Producer<T> producer) {
        if (scope != null && requestContext != null) {
            return scope.runInScope(requestContext, producer);
        } else {
            return producer.call();
        }
    }
}
