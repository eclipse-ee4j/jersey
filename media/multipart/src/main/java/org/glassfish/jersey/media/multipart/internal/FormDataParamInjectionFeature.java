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

package org.glassfish.jersey.media.multipart.internal;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import javax.inject.Provider;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * Feature providing support for {@link org.glassfish.jersey.media.multipart.FormDataParam} parameter injection.
 *
 * @author Michal Gajdos
 */
@ConstrainedTo(RuntimeType.SERVER)
public final class FormDataParamInjectionFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                Provider<MultivaluedParameterExtractorProvider> extractorProvider =
                        createManagedInstanceProvider(MultivaluedParameterExtractorProvider.class);
                Provider<ContainerRequest> requestProvider =
                        createManagedInstanceProvider(ContainerRequest.class);

                FormDataParamValueParamProvider valueSupplier =
                        new FormDataParamValueParamProvider(extractorProvider);
                bind(Bindings.service(valueSupplier).to(ValueParamProvider.class));
                bind(Bindings.injectionResolver(
                        new ParamInjectionResolver<>(valueSupplier, FormDataParam.class, requestProvider)));
            }
        });
        return true;
    }
}
