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

package org.glassfish.jersey.server.microprofile;

import javax.ws.rs.JAXRS.Configuration;
import javax.ws.rs.ProcessingException;

import org.eclipse.microprofile.config.Config;
import org.glassfish.jersey.server.spi.Configurator;

/**
 * Configurator for Microprofile Config configurations.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.28
 */
public class MicroprofileConfigurator implements Configurator {

    /**
     * Configures the configuration builder according to the provided configuration. Just does nothing if the type of
     * configuration is other than {@link Config}.
     *
     * @param configurationBuilder The configuration builder to configure.
     * @param configuration The configuration to use for configuring the configuration builder.
     */
    @Override
    public void configure(final Configuration.Builder configurationBuilder, final Object configuration)
            throws ProcessingException {
        if (configuration instanceof Config) {
            configurationBuilder.from(((Config) configuration)::getOptionalValue);
        }
    }

}
