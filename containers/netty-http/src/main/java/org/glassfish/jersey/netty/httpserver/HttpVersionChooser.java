/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.httpserver;

import java.net.URI;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.Http2Codec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Choose the handler implementation based on Http protocol.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
class HttpVersionChooser extends ApplicationProtocolNegotiationHandler {

    private final URI baseUri;
    private final NettyHttpContainer container;

    HttpVersionChooser(URI baseUri, NettyHttpContainer container) {
        super(ApplicationProtocolNames.HTTP_1_1);

        this.baseUri = baseUri;
        this.container = container;
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            ctx.pipeline().addLast(new Http2Codec(true, new JerseyHttp2ServerHandler(baseUri, container)));
            return;
        }

        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            ctx.pipeline().addLast(new HttpServerCodec(),
                                   new ChunkedWriteHandler(),
                                   new JerseyServerHandler(baseUri, container));
            return;
        }

        throw new IllegalStateException("Unknown protocol: " + protocol);
    }
}
