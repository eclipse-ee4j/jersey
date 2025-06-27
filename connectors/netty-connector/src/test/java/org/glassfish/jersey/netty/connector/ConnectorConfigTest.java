/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class ConnectorConfigTest {
    @Test
    public void testPrecedence() {

        NettyConnectorProvider.Config.RW builderLower = NettyConnectorProvider.config().rw();
        builderLower.maxTotalConnection(55);

        NettyConnectorProvider.Config.RW builderUpper = builderLower.copy();
        builderUpper.maxTotalConnection(56);
        Assertions.assertEquals(56, builderUpper.maxPoolSizeTotal.get());

        Client client = ClientBuilder.newClient();
        client.property(NettyClientProperties.MAX_CONNECTIONS_TOTAL, 57);
        NettyConnectorProvider.Config.RW result = builderUpper.fromClient(client);
        Assertions.assertEquals(57, result.maxPoolSizeTotal.get());
        Assertions.assertEquals(60, result.maxPoolIdle.get());
    }
}
