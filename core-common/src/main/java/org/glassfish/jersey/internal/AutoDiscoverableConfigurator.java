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

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;

/**
 * Configurator which initializes and register {@link AutoDiscoverable} instances into {@link InjectionManager} and
 * {@link BootstrapBag}.
 *
 * @author Petr Bouda
 */
public class AutoDiscoverableConfigurator extends AbstractServiceFinderConfigurator<AutoDiscoverable> {

    /**
     * Create a new configurator.
     *
     * @param runtimeType runtime (client or server) where the service finder binder is used.
     */
    public AutoDiscoverableConfigurator(RuntimeType runtimeType) {
        super(AutoDiscoverable.class, runtimeType);
    }

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        Configuration configuration = bootstrapBag.getConfiguration();
        List<AutoDiscoverable> autoDiscoverables = loadImplementations(configuration.getProperties()).stream()
                .peek(implClass -> injectionManager.register(Bindings.service(implClass).to(AutoDiscoverable.class)))
                .map(injectionManager::createAndInitialize)
                .collect(Collectors.toList());

        bootstrapBag.setAutoDiscoverables(autoDiscoverables);
    }
}
