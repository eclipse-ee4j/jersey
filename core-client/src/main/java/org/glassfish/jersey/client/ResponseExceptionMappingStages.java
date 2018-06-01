/*
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.process.internal.AbstractChainableStage;
import org.glassfish.jersey.process.internal.ChainableStage;
import org.glassfish.jersey.process.internal.RequestScope;
import org.glassfish.jersey.process.internal.Stage;

class ResponseExceptionMappingStages {

    private ResponseExceptionMappingStages() {
        // Prevents instantiation
    }

    /**
     * Create client response exception mapper stage using the injection
     * manager. May return {@code null}.
     *
     * @param injectionManager injection manager to be used.
     * @return configured response exception mapper stage, or {@code null} in
     * case there are no
     * {@link ResponseExceptionMapper response exception mappers} registered in
     * the injection manager.
     */
    static ChainableStage<ClientResponse> createResponseExceptionMappingStage(InjectionManager injectionManager) {
        RankedComparator<ResponseExceptionMapper> comparator = new RankedComparator<>(RankedComparator.Order.DESCENDING);
        Iterable<ResponseExceptionMapper> responseFilters
                = Providers.getAllProviders(injectionManager, ResponseExceptionMapper.class, comparator);
        return responseFilters.iterator().hasNext() ? new ExceptionMapperStage(responseFilters) : null;
    }

    private static class ExceptionMapperStage extends AbstractChainableStage<ClientResponse> {

        private final Iterable<ResponseExceptionMapper> mappers;

        private ExceptionMapperStage(Iterable<ResponseExceptionMapper> mappers) {
            this.mappers = mappers;
        }

        @Override
        public Stage.Continuation<ClientResponse> apply(ClientResponse responseContext) {
                Map<ResponseExceptionMapper, Integer> mapperPriorityMap = new HashMap<>();
                for (ResponseExceptionMapper mapper : mappers) {
                    if (mapper.handles(responseContext.getStatus(), responseContext.getHeaders())) {
                        mapperPriorityMap.put(mapper, mapper.getPriority());
                    }
                }
                if (mapperPriorityMap.size() > 0) {
                    Map<Optional<Throwable>, Integer> errors = new HashMap<>();
                    ClientRequest clientRequest = responseContext.getRequestContext();
                    ClientRuntime runtime = clientRequest.getClientRuntime();
                    RequestScope requestScope = runtime.getRequestScope();
                    mapperPriorityMap.forEach((m, i) -> {
                        Optional<Throwable> t = Optional.ofNullable(m.toThrowable(
                                new InboundJaxrsResponse(responseContext, requestScope))
                        );
                        errors.put(t, i);
                    });

                    Optional<Throwable> prioritised = Optional.empty();
                    for (Map.Entry<Optional<Throwable>, Integer> errorEntry : errors.entrySet()) {
                        if (errorEntry.getKey().isPresent()) {
                            if (!prioritised.isPresent() || errorEntry.getValue() < errors.get(prioritised)) {
                                prioritised = errorEntry.getKey();
                            }
                        }
                    }

                    if (prioritised.isPresent()) {
                        throw (WebApplicationException) prioritised.get();
                    }
                }
            return Stage.Continuation.of(responseContext, getDefaultNext());
        }

    }

}