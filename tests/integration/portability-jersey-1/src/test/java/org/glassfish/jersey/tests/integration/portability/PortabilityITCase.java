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

package org.glassfish.jersey.tests.integration.portability;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.external.ExternalTestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Martin Matula
 */
public class PortabilityITCase extends JerseyTest {

    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder().build();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testHelloWorld() throws Exception {
        String s = resource().path("helloworld").get(String.class);
        assertEquals("Hello World!", s);
    }

    @Test
    public void testJersey() {
        ClientResponse r = resource().path("jersey").get(ClientResponse.class);
        assertEquals(200, r.getStatus());
        assertEquals("Using Jersey 1.x", r.getEntity(String.class));
    }

    /**
     * The whole project is setup for Jersey 2. Need to get the effective port number
     * from Jersey 2 properties to make Hudson happy.
     *
     * @param defaultPort to use if no other configuration is available
     * @return port number to use by the client
     */
    @Override
    protected int getPort(int defaultPort) {

        String port = System.getProperty("jersey.config.test.container.port");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                throw new TestContainerException("jersey.config.test.container.port with a "
                        + "value of \"" + port + "\" is not a valid integer.", e);
            }
        }

        port = System.getProperty("JERSEY_TEST_PORT");
        if (null != port) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                throw new TestContainerException("JERSEY_TEST_PORT with a "
                        + "value of \"" + port + "\" is not a valid integer.", e);
            }
        }
        return defaultPort;
    }
}
