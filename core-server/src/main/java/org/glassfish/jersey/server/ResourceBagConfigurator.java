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

import java.util.logging.Logger;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.model.Resource;

/**
 * Configurator which initializes and register {@link ResourceBag} instance into {@link BootstrapBag}.
 *
 * @author Petr Bouda
 */
class ResourceBagConfigurator implements BootstrapConfigurator {

    private static final Logger LOGGER = Logger.getLogger(ResourceBagConfigurator.class.getName());

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        ServerBootstrapBag serverBag = (ServerBootstrapBag) bootstrapBag;
        ResourceConfig runtimeConfig = serverBag.getRuntimeConfig();

        final boolean disableValidation = ServerProperties.getValue(runtimeConfig.getProperties(),
                ServerProperties.RESOURCE_VALIDATION_DISABLE,
                Boolean.FALSE,
                Boolean.class);

        final ResourceBag.Builder resourceBagBuilder = new ResourceBag.Builder();

        // Adding programmatic resource models
        for (final Resource programmaticResource : runtimeConfig.getResources()) {
            resourceBagBuilder.registerProgrammaticResource(programmaticResource);
        }

        // Introspecting classes & instances
        for (final Class<?> c : runtimeConfig.getClasses()) {
            try {
                final Resource resource = Resource.from(c, disableValidation);
                if (resource != null) {
                    resourceBagBuilder.registerResource(c, resource);
                }
            } catch (final IllegalArgumentException ex) {
                LOGGER.warning(ex.getMessage());
            }
        }

        for (final Object o : runtimeConfig.getSingletons()) {
            try {
                final Resource resource = Resource.from(o.getClass(), disableValidation);
                if (resource != null) {
                    resourceBagBuilder.registerResource(o, resource);
                }
            } catch (final IllegalArgumentException ex) {
                LOGGER.warning(ex.getMessage());
            }
        }

        serverBag.setResourceBag(resourceBagBuilder.build());
    }
}
