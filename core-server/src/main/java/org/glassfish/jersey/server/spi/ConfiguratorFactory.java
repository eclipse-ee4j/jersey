/*
 * Copyright (c) 2018 Markus KARG. All rights reserved.
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

package org.glassfish.jersey.server.spi;

import javax.ws.rs.JAXRS;
import javax.ws.rs.ProcessingException;

import org.glassfish.jersey.internal.ServiceFinder;

/**
 * Factory for configuring configuration builders using provided configuration instances.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.28
 */
public final class ConfiguratorFactory {

    /**
     * Prevents instantiation.
     */
    private ConfiguratorFactory() {
    }

    /**
     * Configures the configuration builder according to the provided configuration if its type is supported, or just
     * does nothing otherwise.
     * <p>
     * The list of service-providers supporting the {@link Configurator} service-provider will be iterated over, so
     * effectively <em>all</em> compatible configurators will apply the configuration, but in <em>arbitrary</em> order.
     * <p>
     *
     * @param configurationBuilder The configuration builder to configure.
     * @param configuration The configuration to use for configuring the configuration builder.
     * @throws ProcessingException in case the type of configuration is supported but there was any other problem when
     * applying the configuration.
     */
    public static void configure(final JAXRS.Configuration.Builder configurationBuilder, final Object configuration) {
        for (final Configurator configurator : ServiceFinder.find(Configurator.class)) {
            configurator.configure(configurationBuilder, configuration);
        }
    }

}
