/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.sse.internal;

import java.util.function.Function;

import javax.ws.rs.core.Context;
import javax.ws.rs.sse.SseEventSink;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * {@link ValueParamProvider} for binding {@link SseEventSink} to its implementation.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class SseEventSinkValueParamProvider extends AbstractValueParamProvider {

    private final Provider<AsyncContext> asyncContextSupplier;

    /**
     * Constructor.
     *
     * @param mpep multivalued map parameter extractor provider.
     */
    @Inject
    public SseEventSinkValueParamProvider(Provider<MultivaluedParameterExtractorProvider> mpep,
                                          Provider<AsyncContext> asyncContextSupplier) {
        super(mpep, Parameter.Source.CONTEXT);
        this.asyncContextSupplier = asyncContextSupplier;
    }

    @Override
    protected Function<ContainerRequest, SseEventSink> createValueProvider(Parameter parameter) {
        if (parameter == null) {
            return null;
        }

        final Class<?> rawParameterType = parameter.getRawType();
        if (rawParameterType == SseEventSink.class && parameter.isAnnotationPresent(Context.class)) {
            return new SseEventSinkValueSupplier(asyncContextSupplier);
        }
        return null;
    }

    private static final class SseEventSinkValueSupplier implements Function<ContainerRequest, SseEventSink> {

        private final Provider<AsyncContext> asyncContextSupplier;

        public SseEventSinkValueSupplier(Provider<AsyncContext> asyncContextSupplier) {
            this.asyncContextSupplier = asyncContextSupplier;
        }

        @Override
        public SseEventSink apply(ContainerRequest containerRequest) {
            return new JerseyEventSink(asyncContextSupplier);
        }
    }
}
