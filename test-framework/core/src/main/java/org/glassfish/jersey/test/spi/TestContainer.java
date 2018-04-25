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

package org.glassfish.jersey.test.spi;

import java.net.URI;

import org.glassfish.jersey.client.ClientConfig;

/**
 * A test container.
 * <p/>
 * An instance of a test container can be started and stopped only once to prevent test corruption. For another test a new test
 * container instance has to be created.
 *
 * @author Paul Sandoz
 */
public interface TestContainer {

    /**
     * Get a client configuration specific to the test container.
     *
     * @return a client configuration specific to the test container, otherwise {@code null} if there
     *         is no specific client configuration required.
     */
    public ClientConfig getClientConfig();

    /**
     * Get the base URI of the application.
     *
     * @return the base URI of the application.
     */
    public URI getBaseUri();

    /**
     * Start the container.
     */
    public void start();

    /**
     * Stop the container.
     */
    public void stop();
}
