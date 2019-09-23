/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2167;

import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;

/**
 * Custom annotation value supplier provider for JERSEY-2167 reproducer.
 *
 * @author Adam Lindenthal
 */
@Singleton
public class MyValueParamProvider extends AbstractValueParamProvider {

    @Inject
    public MyValueParamProvider(Provider<MultivaluedParameterExtractorProvider> mpep) {
        super(mpep, Parameter.Source.UNKNOWN);
    }

    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        return new MyValueSupplier();
    }

    private static final class MyValueSupplier implements Function<ContainerRequest, Object> {

        @Override
        public Object apply(ContainerRequest request) {
            // returns some testing value
            return "injected timestamp=" + System.currentTimeMillis();
        }
    }
}
