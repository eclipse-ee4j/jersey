/*
 * Copyright (c) 2011, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;

import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.PropertiesHelper;

/**
 * Simple ServiceFinder injection binder.
 *
 * Looks for all implementations of a given contract using {@link ServiceFinder}
 * and registers found instances to {@link InjectionManager}.
 *
 * @param <T> contract type.
 * @author Pavel Bucek
 * @author Libor Kramolis
 */
public class ServiceFinderBinder<T> extends AbstractBinder {

    private final Class<T> contract;

    private final Map<String, Object> applicationProperties;

    private final RuntimeType runtimeType;

    /**
     * Create a new service finder injection binder.
     *
     * @param contract contract of the service providers bound by this binder.
     * @param applicationProperties map containing application properties. May be {@code null}.
     * @param runtimeType runtime (client or server) where the service finder binder is used.
     */
    public ServiceFinderBinder(Class<T> contract, Map<String, Object> applicationProperties, RuntimeType runtimeType) {
        this.contract = contract;
        this.applicationProperties = applicationProperties;
        this.runtimeType = runtimeType;
    }

    @Override
    protected void configure() {
        if (PropertiesHelper.isMetaInfServicesEnabled(applicationProperties, runtimeType)) {
            for (Class<T> t : ServiceFinder.find(contract, true).toClassArray()) {
                bind(t).to(contract);
            }
        }
    }
}
