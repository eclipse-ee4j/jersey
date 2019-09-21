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

package org.glassfish.jersey.server;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.container.ResourceContext;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.server.internal.JerseyResourceContext;

/**
 * Configurator which initializes and register {@link JerseyResourceContext} instance into {@link InjectionManager} and
 * {@link BootstrapBag}.
 *
 * @author Petr Bouda
 */
class JerseyResourceContextConfigurator implements BootstrapConfigurator {

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        ServerBootstrapBag serverBag = (ServerBootstrapBag) bootstrapBag;
        Consumer<Binding> registerBinding = injectionManager::register;
        Function<Class<?>, ?> getOrCreateInstance = clazz -> Injections.getOrCreate(injectionManager, clazz);
        Consumer<Object> injectInstance = injectionManager::inject;

        // Initialize and register Resource Context
        JerseyResourceContext resourceContext = new JerseyResourceContext(getOrCreateInstance, injectInstance, registerBinding);
        InstanceBinding<JerseyResourceContext> resourceContextBinding =
                Bindings.service(resourceContext)
                        .to(ResourceContext.class)
                        .to(ExtendedResourceContext.class);
        injectionManager.register(resourceContextBinding);
        serverBag.setResourceContext(resourceContext);
    }

    @Override
    public void postInit(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
    }
}
