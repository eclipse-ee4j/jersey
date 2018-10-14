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

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.JAXRS;
import javax.ws.rs.JAXRS.Configuration;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.spi.Contract;

/**
 * Service-provider interface for configuring {@link Configuration.Builder} instances.
 * <p>
 * If the type of supported configuration is supported by the provider, the builder instance will get configured
 * accordingly.
 * </p>
 * <p>
 * An implementation can identify itself by placing a Java service provider configuration file (if not already present)
 * - {@code org.glassfish.jersey.server.spi.Configurator} - in the resource directory {@code META-INF/services}, and
 * adding the fully qualified service-provider-class of the implementation in the file.
 * </p>
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.28
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface Configurator {

    /**
     * Configures the configuration builder according to the provided configuration if its type is supported, or just does
     * nothing otherwise.
     *
     * @param configurationBuilder The configuration builder to configure.
     * @param configuration The configuration to use for configuring the configuration builder.
     * @throws ProcessingException in case the type of configuration is supported but there was any other problem when
     * applying the configuration.
     */
    public void configure(JAXRS.Configuration.Builder configurationBuilder, Object configuration) throws ProcessingException;

}
