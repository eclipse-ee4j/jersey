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

import org.glassfish.jersey.test.DeploymentContext;

/**
 * A test container factory responsible for creating test containers.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface TestContainerFactory {

    /**
     * Create a test container instance.
     *
     * @param baseUri base URI for the test container to run at.
     * @param deploymentContext deployment context of the tested JAX-RS / Jersey application .
     * @return new test container configured to run the tested application.
     *
     * @throws IllegalArgumentException if {@code deploymentContext} is not supported
     *                                  by this test container factory.
     */
    TestContainer create(URI baseUri, DeploymentContext deploymentContext);
}
