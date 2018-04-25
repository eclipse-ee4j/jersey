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

package org.glassfish.jersey.server.internal.process;

import java.util.function.Function;

import javax.inject.Provider;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;

/**
 * Request/response scoped injection support initialization stage.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class ReferencesInitializer implements Function<RequestProcessingContext, RequestProcessingContext> {

    private final InjectionManager injectionManager;
    private final Provider<RequestProcessingContextReference> processingContextRefProvider;

    /**
     * Injection constructor.
     *
     * @param injectionManager application injection manager.
     * @param processingContextRefProvider container request reference provider (request-scoped).
     */
    public ReferencesInitializer(
            InjectionManager injectionManager, Provider<RequestProcessingContextReference> processingContextRefProvider) {
        this.injectionManager = injectionManager;
        this.processingContextRefProvider = processingContextRefProvider;
    }

    /**
     * Initialize the request references using the incoming request processing context.
     *
     *
     * @param context incoming request context.
     * @return same (unmodified) request context.
     */
    @Override
    public RequestProcessingContext apply(final RequestProcessingContext context) {
        processingContextRefProvider.get().set(context);

        final RequestScopedInitializer requestScopedInitializer = context.request().getRequestScopedInitializer();
        if (requestScopedInitializer != null) {
            requestScopedInitializer.initialize(injectionManager);
        }

        return context;
    }
}
