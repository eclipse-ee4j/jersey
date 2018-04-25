/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.memleak.common;

import java.io.IOException;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.After;

/**
 * Adds support for web application testing and memory leak detection in target JVM.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class AbstractMemoryLeakWebAppTest extends JerseyTest {

    private static final String CONTEXT_ROOT = System.getProperty(MemoryLeakUtils.JERSEY_CONFIG_TEST_CONTAINER_CONTEXT_ROOT, "/");

    /**
     * Verifies no OutOfMemoryError is present in associated log file.<br/> The motivation is to have the OutOfMemory error
     * included in the JUnit test result if possible.<br/> The problem is that even of OutOfMemoryError occurred, it may not be
     * logged yet. Therefore the log file needs to be inspected when the JVM is shut down and all its resources are flushed and
     * closed.
     *
     * @throws IOException
     */
    @After
    public void verifyNoOutOfMemoryOccurred() throws IOException {
        MemoryLeakUtils.verifyNoOutOfMemoryOccurred();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        return DeploymentContext.builder(configure()).contextPath(CONTEXT_ROOT).build();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config
                .property(ClientProperties.CONNECT_TIMEOUT, 30000)
                .property(ClientProperties.READ_TIMEOUT, 30000);
    }

}
