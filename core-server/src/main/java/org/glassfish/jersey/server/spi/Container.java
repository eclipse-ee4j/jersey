/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
import javax.ws.rs.RuntimeType;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.spi.Contract;

/**
 * Jersey container service contract.
 *
 * The purpose of the container is to configure and host a single Jersey
 * application.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 *
 * @see org.glassfish.jersey.server.ApplicationHandler
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface Container {

    /**
     * Default container port number for HTTP protocol.
     *
     * @since 2.18
     */
    public static final int DEFAULT_HTTP_PORT = 80;
    /**
     * Default container port number for HTTPS protocol.
     *
     * @since 2.18
     */
    public static final int DEFAULT_HTTPS_PORT = 443;

    /**
     * Return an immutable representation of the current {@link ResourceConfig
     * configuration}.
     *
     * @return current configuration of the hosted Jersey application.
     */
    public ResourceConfig getConfiguration();

    /**
     * Get the Jersey server-side application handler associated with the container.
     *
     * @return Jersey server-side application handler associated with the container.
     */
    public ApplicationHandler getApplicationHandler();

    /**
     * Reload the hosted Jersey application using the current {@link ResourceConfig
     * configuration}.
     */
    public void reload();

    /**
     * Reload the hosted Jersey application using a new {@link ResourceConfig
     * configuration}.
     *
     * @param configuration new configuration used for the reload.
     */
    public void reload(ResourceConfig configuration);
}
