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

package org.glassfish.jersey.server.model;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import org.glassfish.jersey.model.ContractProvider;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * Default {@link javax.ws.rs.core.Configuration configuration} for resource methods.
 * The only allowed contract types for this configuration are:
 * <ul>
 * <li>ContainerRequestFilter</li>
 * <li>ContainerResponseFilter</li>
 * <li>ReaderInterceptor</li>
 * <li>WriterInterceptor</li>
 * </ul>
 *
 * @author Michal Gajdos
 */
class ResourceMethodConfig extends CommonConfig {

    private static final Logger LOGGER = Logger.getLogger(ResourceMethodConfig.class.getName());

    private static final Set<Class<?>> allowedContracts;

    static {
        //noinspection unchecked
        Set<Class<?>> tempSet = Collections.newSetFromMap(new IdentityHashMap<>());
        tempSet.add(ContainerRequestFilter.class);
        tempSet.add(ContainerResponseFilter.class);
        tempSet.add(ReaderInterceptor.class);
        tempSet.add(WriterInterceptor.class);
        allowedContracts = Collections.unmodifiableSet(tempSet);
    }

    /**
     * Create new resource method runtime configuration.
     *
     * @param properties inherited properties.
     */
    ResourceMethodConfig(final Map<String, Object> properties) {
        super(RuntimeType.SERVER, ComponentBag.EXCLUDE_EMPTY);
        setProperties(properties);
    }

    @Override
    protected Inflector<ContractProvider.Builder, ContractProvider> getModelEnhancer(final Class<?> providerClass) {
        return new Inflector<ContractProvider.Builder, ContractProvider>() {
            @Override
            public ContractProvider apply(ContractProvider.Builder builder) {
                for (Iterator<Class<?>> it =  builder.getContracts().keySet().iterator(); it.hasNext(); ) {
                    final Class<?> contract = it.next();
                    if (!allowedContracts.contains(contract)) {
                        LOGGER.warning(LocalizationMessages.CONTRACT_CANNOT_BE_BOUND_TO_RESOURCE_METHOD(contract, providerClass));
                        it.remove();
                    }
                }

                return builder.build();
            }
        };
    }
}
