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

import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import javax.inject.Provider;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.model.internal.RankedComparator;

/**
 * Function that can be put to an acceptor chain to properly initialize
 * the client-side request-scoped processing injection for the current
 * request and response exchange.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class RequestProcessingInitializationStage implements Function<ClientRequest, ClientRequest> {
    private final Provider<Ref<ClientRequest>> requestRefProvider;
    private final MessageBodyWorkers workersProvider;
    private final Iterable<WriterInterceptor> writerInterceptors;
    private final Iterable<ReaderInterceptor> readerInterceptors;

    /**
     * Create new {@link org.glassfish.jersey.message.MessageBodyWorkers} initialization function
     * for requests and responses.
     *
     * @param requestRefProvider client request context reference injection provider.
     * @param workersProvider message body workers injection provider.
     * @param injectionManager injection manager.
     */
    public RequestProcessingInitializationStage(
            Provider<Ref<ClientRequest>> requestRefProvider,
            MessageBodyWorkers workersProvider,
            InjectionManager injectionManager) {
        this.requestRefProvider = requestRefProvider;
        this.workersProvider = workersProvider;
        writerInterceptors = Collections.unmodifiableList(
                StreamSupport.stream(
                        Providers.getAllProviders(injectionManager, WriterInterceptor.class,
                                new RankedComparator<>()).spliterator(), false)
                             .collect(Collectors.toList())
        );
        readerInterceptors = Collections.unmodifiableList(
                StreamSupport.stream(
                        Providers.getAllProviders(injectionManager, ReaderInterceptor.class,
                                new RankedComparator<>()).spliterator(), false)
                             .collect(Collectors.toList())
        );
    }

    @Override
    public ClientRequest apply(ClientRequest requestContext) {
        requestRefProvider.get().set(requestContext);
        requestContext.setWorkers(workersProvider);
        requestContext.setWriterInterceptors(writerInterceptors);
        requestContext.setReaderInterceptors(readerInterceptors);

        return requestContext;
    }
}
