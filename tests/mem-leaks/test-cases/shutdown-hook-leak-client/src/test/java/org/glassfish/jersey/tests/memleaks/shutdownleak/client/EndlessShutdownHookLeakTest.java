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

package org.glassfish.jersey.tests.memleaks.shutdownleak.client;

import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.test.memleak.common.AbstractMemoryLeakSimpleTest;
import org.glassfish.jersey.test.memleak.common.MemoryLeakSucceedingTimeout;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * Reproducer for JERSEY-2786.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class EndlessShutdownHookLeakTest extends AbstractMemoryLeakSimpleTest {

    private static final Logger LOGGER = Logger.getLogger(EndlessShutdownHookLeakTest.class.getName());

    @Rule
    public Timeout globalTimeout = new MemoryLeakSucceedingTimeout();

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target("http://example.com");

    public EndlessShutdownHookLeakTest() {
        LOGGER.fine("Preparing...");
        for (int i = 0; i < 1000; ++i) {
            shutdownHookLeakIteration();
        }
    }

    @Test
    public void testShutdownHookDoesNotLeak() throws Exception {
        while (true) {
            shutdownHookLeakIteration();
        }
    }

    private void shutdownHookLeakIteration() {
        System.out.print(".");
        WebTarget target2 = target.property("Washington", "Irving");
        Builder req = target2.request().property("how", "now");
        req.buildGet().property("Irving", "Washington");
    }

}
