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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2Codec;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AsciiString;

/**
 * Jersey {@link ChannelInitializer}.
 * <p>
 * Adds {@link HttpServerCodec}, {@link ChunkedWriteHandler} and {@link JerseyServerHandler} to the channels pipeline.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
class JerseyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final URI baseUri;
    private final SslContext sslCtx;
    private final NettyHttpContainer container;
    private final boolean http2;

    /**
     * Constructor.
     *
     * @param baseUri   base {@link URI} of the container (includes context path, if any).
     * @param sslCtx    SSL context.
     * @param container Netty container implementation.
     */
    public JerseyServerInitializer(URI baseUri, SslContext sslCtx, NettyHttpContainer container) {
        this(baseUri, sslCtx, container, false);
    }

    /**
     * Constructor.
     *
     * @param baseUri   base {@link URI} of the container (includes context path, if any).
     * @param sslCtx    SSL context.
     * @param container Netty container implementation.
     * @param http2     Http/2 protocol support.
     */
    public JerseyServerInitializer(URI baseUri, SslContext sslCtx, NettyHttpContainer container, boolean http2) {
        this.baseUri = baseUri;
        this.sslCtx = sslCtx;
        this.container = container;
        this.http2 = http2;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        if (http2) {

            if (sslCtx != null) {
                configureSsl(ch);
            } else {
                configureClearText(ch);
            }

        } else {
            ChannelPipeline p = ch.pipeline();
            if (sslCtx != null) {
                p.addLast(sslCtx.newHandler(ch.alloc()));
            }
            p.addLast(new HttpServerCodec());
            p.addLast(new ChunkedWriteHandler());
            p.addLast(new JerseyServerHandler(baseUri, container));
        }
    }

    /**
     * Configure the pipeline for TLS NPN negotiation to HTTP/2.
     */
    private void configureSsl(SocketChannel ch) {
        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()), new HttpVersionChooser(baseUri, container));
    }

    /**
     * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.
     */
    private void configureClearText(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline();
        final HttpServerCodec sourceCodec = new HttpServerCodec();

        p.addLast(sourceCodec);
        p.addLast(new HttpServerUpgradeHandler(sourceCodec, new HttpServerUpgradeHandler.UpgradeCodecFactory() {
            @Override
            public HttpServerUpgradeHandler.UpgradeCodec newUpgradeCodec(CharSequence protocol) {
                if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
                    return new Http2ServerUpgradeCodec(new Http2Codec(true, new JerseyHttp2ServerHandler(baseUri, container)));
                } else {
                    return null;
                }
            }
        }));
        p.addLast(new SimpleChannelInboundHandler<HttpMessage>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) throws Exception {
                // If this handler is hit then no upgrade has been attempted and the client is just talking HTTP.
                // "Directly talking: " + msg.protocolVersion() + " (no upgrade was attempted)");

                ChannelPipeline pipeline = ctx.pipeline();
                ChannelHandlerContext thisCtx = pipeline.context(this);
                pipeline.addAfter(thisCtx.name(), null, new JerseyServerHandler(baseUri, container));
                pipeline.replace(this, null, new ChunkedWriteHandler());
                ctx.fireChannelRead(msg);
            }
        });
    }
}
