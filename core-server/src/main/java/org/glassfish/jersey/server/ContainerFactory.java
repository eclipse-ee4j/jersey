/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.core.Application;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.server.spi.ContainerProvider;

/**
 * Factory for creating specific HTTP-based containers.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class ContainerFactory {

    /**
     * Prevents instantiation.
     */
    private ContainerFactory() {
    }

    /**
     * Create a container according to the class requested.
     * <p>
     * The list of service-provider supporting the {@link ContainerProvider}
     * service-provider will be iterated over until one returns a non-null
     * container instance.
     * <p>
     *
     * @param <T>         container type
     * @param type        type of the container
     * @param application JAX-RS / Jersey application.
     * @return the container.
     *
     * @throws ContainerException       if there was an error creating the container.
     * @throws IllegalArgumentException if no container provider supports the type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createContainer(final Class<T> type, final Application application) {
        for (final ContainerProvider containerProvider : ServiceFinder.find(ContainerProvider.class)) {
            final T container = containerProvider.createContainer(type, application);
            if (container != null) {
                return container;
            }
        }

        throw new IllegalArgumentException("No container provider supports the type " + type);
    }

}
