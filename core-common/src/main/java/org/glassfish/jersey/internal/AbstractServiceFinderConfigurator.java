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

package org.glassfish.jersey.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * Simple ServiceFinder configuration.
 *
 * Looks for all implementations of a given contract using {@link ServiceFinder} and registers found instances to
 * {@link InjectionManager}.
 *
 * @param <T> contract type.
 * @author Petr Bouda
 */
public abstract class AbstractServiceFinderConfigurator<T> implements BootstrapConfigurator {

    private final Class<T> contract;
    private final RuntimeType runtimeType;

    /**
     * Create a new configurator.
     *
     * @param contract    contract of the service providers bound by this binder.
     * @param runtimeType runtime (client or server) where the service finder binder is used.
     */
    protected AbstractServiceFinderConfigurator(Class<T> contract, RuntimeType runtimeType) {
        this.contract = contract;
        this.runtimeType = runtimeType;
    }

    /**
     * Load all particular implementations of the type {@code T} using {@link ServiceFinder}.
     *
     * @param applicationProperties map containing application properties. May be {@code null}
     * @return all registered classes of the type {@code T}.
     */
    protected List<Class<T>> loadImplementations(Map<String, Object> applicationProperties) {
        boolean METAINF_SERVICES_LOOKUP_DISABLE_DEFAULT = false;
        boolean disableMetaInfServicesLookup = METAINF_SERVICES_LOOKUP_DISABLE_DEFAULT;
        if (applicationProperties != null) {
            disableMetaInfServicesLookup = CommonProperties.getValue(applicationProperties, runtimeType,
                    CommonProperties.METAINF_SERVICES_LOOKUP_DISABLE, METAINF_SERVICES_LOOKUP_DISABLE_DEFAULT, Boolean.class);
        }
        if (!disableMetaInfServicesLookup) {
            return Stream.of(ServiceFinder.find(contract, true).toClassArray())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
