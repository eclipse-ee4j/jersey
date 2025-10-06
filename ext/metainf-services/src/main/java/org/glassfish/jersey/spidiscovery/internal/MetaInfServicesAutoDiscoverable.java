/*
 * Copyright (c) 2014, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.spidiscovery.internal;

import java.util.Map;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

import jakarta.annotation.Priority;

import org.glassfish.jersey.innate.inject.InternalBinder;
import org.glassfish.jersey.internal.ServiceFinderBinder;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;

/**
 * @author Michal Gajdos
 */
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public class MetaInfServicesAutoDiscoverable implements ForcedAutoDiscoverable {

    @Override
    public void configure(final FeatureContext context) {
        final Map<String, Object> properties = context.getConfiguration().getProperties();
        final RuntimeType runtimeType = context.getConfiguration().getRuntimeType();

        context.register(new InternalBinder() {
            @Override
            protected void configure() {
                // Message Body providers.
                install(new ServiceFinderBinder<MessageBodyReader>(MessageBodyReader.class, properties, runtimeType));
                install(new ServiceFinderBinder<MessageBodyWriter>(MessageBodyWriter.class, properties, runtimeType));
                // Exception Mappers.
                install(new ServiceFinderBinder<ExceptionMapper>(ExceptionMapper.class, properties, runtimeType));
            }
        });
    }
}
