/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.internal.routing.ClientResponseMediaTypeDeterminer;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.process.internal.AbstractChainableStage;
import org.glassfish.jersey.process.internal.ChainableStage;

/**
 * Client filtering stage factory.
 *
 * @author Marek Potociar
 */
class ClientFilteringStages {

    private ClientFilteringStages() {
        // Prevents instantiation
    }

    /**
     * Create client request filtering stage using the injection manager. May return {@code null}.
     *
     * @param injectionManager injection manager to be used.
     * @return configured request filtering stage, or {@code null} in case there are no
     *         {@link ClientRequestFilter client request filters} registered in the injection manager.
     */
    static ChainableStage<ClientRequest> createRequestFilteringStage(InjectionManager injectionManager) {
        RankedComparator<ClientRequestFilter> comparator = new RankedComparator<>(RankedComparator.Order.ASCENDING);
        Iterable<ClientRequestFilter> requestFilters =
                Providers.getAllProviders(injectionManager, ClientRequestFilter.class, comparator);
        return requestFilters.iterator().hasNext() ? new RequestFilteringStage(requestFilters) : null;
    }

    /**
     * Create client request filtering stage using the injection manager. May return {@code null}.
     *
     * @param firstFilter Non null {@link ClientRequestFilter client request filter} to be executed
     *                    in the client request filtering stage.
     * @param injectionManager injection manager to be used.
     * @return configured request filtering stage, or {@code null} in case there are no
     *         {@link ClientRequestFilter client request filters} registered in the injection manager
     *         and {@code firstFilter} is null.
     */
    static ChainableStage<ClientRequest> createRequestFilteringStage(ClientRequestFilter firstFilter,
                                                                     InjectionManager injectionManager) {
        RankedComparator<ClientRequestFilter> comparator = new RankedComparator<>(RankedComparator.Order.ASCENDING);
        Iterable<ClientRequestFilter> requestFilters =
                Providers.getAllProviders(injectionManager, ClientRequestFilter.class, comparator);
        if (firstFilter != null && !requestFilters.iterator().hasNext()) {
            return new RequestFilteringStage(Collections.singletonList(firstFilter));
        } else if (firstFilter != null && requestFilters.iterator().hasNext()) {
            return new RequestFilteringStage(prependFilter(firstFilter, requestFilters));
        }
        return null;
    }

    /**
     * Create client response filtering stage using the injection manager. May return {@code null}.
     *
     * @param injectionManager injection manager to be used.
     * @return configured response filtering stage, or {@code null} in case there are no
     *         {@link ClientResponseFilter client response filters} registered in the injection manager.
     */
    static ChainableStage<ClientResponse> createResponseFilteringStage(InjectionManager injectionManager) {
        RankedComparator<ClientResponseFilter> comparator = new RankedComparator<>(RankedComparator.Order.DESCENDING);
        Iterable<ClientResponseFilter> responseFilters =
                Providers.getAllProviders(injectionManager, ClientResponseFilter.class, comparator);
        return responseFilters.iterator().hasNext() ? new ResponseFilterStage(responseFilters) : null;
    }

    /**
     * Prepend an filter to a given iterable.
     * @param filter to be prepend.
     * @param filters the iterable the given filter is to be prependto
     * @param <T> filter type
     * @return iterable with first item of prepended filter.
     */
    private static <T> Iterable<T> prependFilter(T filter, Iterable<T> filters) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    final Iterator<T> filterIterator = filters.iterator();
                    boolean wasInterceptorFilterNext = false;

                    @Override
                    public boolean hasNext() {
                        return !wasInterceptorFilterNext || filterIterator.hasNext();
                    }

                    @Override
                    public T next() {
                        if (wasInterceptorFilterNext) {
                            return filterIterator.next();
                        } else {
                            wasInterceptorFilterNext = true;
                            return filter;
                        }
                    }
                };
            }
        };
    }

    private static final class RequestFilteringStage extends AbstractChainableStage<ClientRequest> {

        private final Iterable<ClientRequestFilter> requestFilters;

        private RequestFilteringStage(final Iterable<ClientRequestFilter> requestFilters) {
            this.requestFilters = requestFilters;
        }

        @Override
        public Continuation<ClientRequest> apply(ClientRequest requestContext) {
            for (ClientRequestFilter filter : requestFilters) {
                try {
                    filter.filter(requestContext);
                    final Response abortResponse = requestContext.getAbortResponse();
                    if (abortResponse != null) {
                        final ClientResponseMediaTypeDeterminer determiner = new ClientResponseMediaTypeDeterminer(
                                requestContext.getWorkers());
                        determiner.setResponseMediaTypeIfNotSet(abortResponse, requestContext.getConfiguration());
                        throw new AbortException(new ClientResponse(requestContext, abortResponse));
                    }
                } catch (IOException ex) {
                    throw new ProcessingException(ex);
                }
            }
            return Continuation.of(requestContext, getDefaultNext());
        }
    }

    private static class ResponseFilterStage extends AbstractChainableStage<ClientResponse> {

        private final Iterable<ClientResponseFilter> filters;

        private ResponseFilterStage(Iterable<ClientResponseFilter> filters) {
            this.filters = filters;
        }

        @Override
        public Continuation<ClientResponse> apply(ClientResponse responseContext) {
            try {
                for (ClientResponseFilter filter : filters) {
                    filter.filter(responseContext.getRequestContext(), responseContext);
                }
            } catch (IOException ex) {
                InboundJaxrsResponse response = new InboundJaxrsResponse(responseContext, null);
                throw new ResponseProcessingException(response, ex);
            }

            return Continuation.of(responseContext, getDefaultNext());
        }
    }
}
