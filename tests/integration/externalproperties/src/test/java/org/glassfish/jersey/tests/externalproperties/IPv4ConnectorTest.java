/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.externalproperties;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;
import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class IPv4ConnectorTest extends JerseyTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<Object[]> connectors() {
        return Arrays.asList(new Object[][]{
                {HttpUrlConnectorProvider.class},
                {GrizzlyConnectorProvider.class},
                {JettyConnectorProvider.class},
                {ApacheConnectorProvider.class},
                {GrizzlyConnectorProvider.class},
                {NettyConnectorProvider.class},
                {JdkConnectorProvider.class},
        });
    }

    private final ConnectorProvider connectorProvider;

    public IPv4ConnectorTest(Class<? extends ConnectorProvider> connectorProviderClass)
            throws IllegalAccessException, InstantiationException {
        this.connectorProvider = connectorProviderClass.newInstance();
    }

    @Override
    protected Application configure() {
        return new ResourceConfig();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(connectorProvider);
    }

    @Test
    public void testIPv4Address() {
        try  {
            target().request().get();
        } catch (ProcessingException pe) {
            if (connectorProvider instanceof JettyConnectorProvider) {
                Assert.assertEquals("java.net.SocketException: Could not connect to /0:0:0:0:0:0:0:1:9997",
                        pe.getCause().getMessage());
            } else {
                Assert.assertTrue(pe.getCause().getMessage().contains("Protocol family unavailable"));
            }
        }
    }
}
