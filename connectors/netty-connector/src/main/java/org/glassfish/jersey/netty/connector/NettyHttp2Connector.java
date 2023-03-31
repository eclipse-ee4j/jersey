/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

import io.netty.channel.ChannelInitializer;
import javax.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.innate.ClientProxy;

import java.net.URI;
import java.util.Optional;

public class NettyHttp2Connector extends NettyConnector {

    NettyHttp2Connector(Client client) {
        super(client);
    }


    @Override
    public ChannelInitializer provideChannelInitializer(ClientRequest jerseyRequest,
                                                       Optional<ClientProxy> handlerProxy,
                                                       long connectTimeout,
                                                       URI requestUri) {
        return new Http2ClientInitializer(jerseyRequest, handlerProxy);
    }
}
