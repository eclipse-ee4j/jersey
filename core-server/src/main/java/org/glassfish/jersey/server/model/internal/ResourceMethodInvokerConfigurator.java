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

package org.glassfish.jersey.server.model.internal;

import java.util.Arrays;
import java.util.List;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ServerBootstrapBag;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.ResourceMethodInvoker;
import org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher;

/**
 * Configurator which initializes and register {@link ResourceMethodDispatcher} instance into {@link BootstrapBag}.
 *
 * @author Petr Bouda
 */
public class ResourceMethodInvokerConfigurator implements BootstrapConfigurator {

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
    }

    @Override
    public void postInit(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        ServerBootstrapBag serverBag = (ServerBootstrapBag) bootstrapBag;

        List<ResourceMethodDispatcher.Provider> providers = Arrays.asList(
                new VoidVoidDispatcherProvider(serverBag.getResourceContext()),
                new JavaResourceMethodDispatcherProvider(serverBag.getValueParamProviders()));

        ResourceMethodInvoker.Builder builder = new ResourceMethodInvoker.Builder()
                .injectionManager(injectionManager)
                .resourceMethodDispatcherFactory(new ResourceMethodDispatcherFactory(providers))
                .resourceMethodInvocationHandlerFactory(new ResourceMethodInvocationHandlerFactory(injectionManager))
                .configuration(bootstrapBag.getConfiguration())
                .configurationValidator(() -> injectionManager.getInstance(ConfiguredValidator.class));

        serverBag.setResourceMethodInvokerBuilder(builder);
    }
}
