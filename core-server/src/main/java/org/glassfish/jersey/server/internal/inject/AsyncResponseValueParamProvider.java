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

package org.glassfish.jersey.server.internal.inject;

import java.util.function.Function;

import javax.ws.rs.container.AsyncResponse;

import javax.inject.Provider;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * Value factory provider supporting the {@link javax.ws.rs.container.Suspended} injection annotation.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
final class AsyncResponseValueParamProvider implements ValueParamProvider {

    private final Provider<AsyncContext> asyncContextProvider;

    /**
     * Initialize the provider.
     *
     * @param asyncContextProvider async processing context provider.
     */
    public AsyncResponseValueParamProvider(Provider<AsyncContext> asyncContextProvider) {
        this.asyncContextProvider = asyncContextProvider;
    }

    @Override
    public Function<ContainerRequest, AsyncResponse> getValueProvider(final Parameter parameter) {
        if (parameter.getSource() != Parameter.Source.SUSPENDED) {
            return null;
        }
        if (!AsyncResponse.class.isAssignableFrom(parameter.getRawType())) {
            return null;
        }

        return containerRequest -> asyncContextProvider.get();
    }

    @Override
    public PriorityType getPriority() {
        return Priority.NORMAL;
    }
}
