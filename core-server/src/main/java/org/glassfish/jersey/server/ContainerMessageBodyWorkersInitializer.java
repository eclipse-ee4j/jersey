/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.internal.process.RequestProcessingContext;

/**
 * Function that can be put to an acceptor chain to properly initialize
 * {@link org.glassfish.jersey.message.MessageBodyWorkers} instance on a current request and response.
 *
 * @author Marek Potociar
 */
public class ContainerMessageBodyWorkersInitializer
        implements Function<RequestProcessingContext, RequestProcessingContext> {
    private final Provider<MessageBodyWorkers> workersFactory;

    /**
     * Create new {@link org.glassfish.jersey.message.MessageBodyWorkers} initialization function for requests
     * and responses.
     *
     * @param workersFactory {@code MessageBodyWorkers} factory.
     */
    @Inject
    public ContainerMessageBodyWorkersInitializer(
            Provider<MessageBodyWorkers> workersFactory) {
        this.workersFactory = workersFactory;
    }


    @Override
    public RequestProcessingContext apply(RequestProcessingContext requestContext) {
        requestContext.request().setWorkers(workersFactory.get());

        return requestContext;
    }
}
