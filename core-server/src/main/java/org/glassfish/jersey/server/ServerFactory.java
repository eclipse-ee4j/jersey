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

package org.glassfish.jersey.server;

import jakarta.ws.rs.JAXRS;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.server.spi.Server;
import org.glassfish.jersey.server.spi.ServerProvider;

/**
 * Factory for creating specific HTTP servers.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.30
 */
public final class ServerFactory {

    /**
     * Prevents instantiation.
     */
    private ServerFactory() {
    }

    /**
     * Creates a server of a given type which runs the given application using the
     * given bootstrap configuration.
     * <p>
     * The list of service-providers supporting the {@link ServerProvider}
     * service-provider will be iterated over until one returns a non-null server
     * instance.
     * <p>
     *
     * @param <T>
     *            the type of the server.
     * @param type
     *            the type of the server. Providers SHOULD support at least
     *            {@link Server}.
     * @param application
     *            The application to host.
     * @param configuration
     *            The configuration (host, port, etc.) to be used for bootstrapping.
     * @return the created server.
     * @throws ProcessingException
     *             if there is an error creating the server.
     * @throws IllegalArgumentException
     *             if no server provider supports the type.
     */
    public static <T extends Server> T createServer(final Class<T> type, final Application application,
            final JAXRS.Configuration configuration) {
        for (final ServerProvider serverProvider : ServiceFinder.find(ServerProvider.class)) {
            final T server = serverProvider.createServer(type, application, configuration);
            if (server != null) {
                return server;
            }
        }

        throw new IllegalArgumentException("No server provider supports the type " + type);
    }

}
