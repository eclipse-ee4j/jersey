/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet.internal;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.servlet.internal.spi.ServletContainerProvider;

/**
 * Factory class to get all "registered" implementations of {@link ServletContainerProvider}.
 * Mentioned implementation classes are registered by {@code META-INF/services}.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 * @since 2.1
 */
public final class ServletContainerProviderFactory {

    private ServletContainerProviderFactory() {
    }

    /**
     * Returns array of all "registered" implementations of {@link ServletContainerProvider}.
     *
     * @return Array of registered providers. Never returns {@code null}.
     *         If there is no implementation registered empty array is returned.
     */
    public static ServletContainerProvider[] getAllServletContainerProviders() {
        // TODO Instances of ServletContainerProvider could be cached, maybe. ???
        // TODO Check if META-INF/services lookup is enabled. ???
        return ServiceFinder.find(ServletContainerProvider.class).toArray();
    }

}
