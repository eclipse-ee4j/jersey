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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import javax.ws.rs.core.SecurityContext;

import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.netty.connector.internal.NettyInputStream;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.ContainerUtils;

/**
 * Jersey Netty HTTP/2 handler.
 * <p>
 * Note that this implementation cannot be more experimental. Any contributions / feedback is welcomed.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@ChannelHandler.Sharable
class JerseyHttp2ServerHandler extends ChannelDuplexHandler {

    private final URI baseUri;
    private final LinkedBlockingDeque<InputStream> isList = new LinkedBlockingDeque<>();
    private final NettyHttpContainer container;

    /**
     * Constructor.
     *
     * @param baseUri   base {@link URI} of the container (includes context path, if any).
     * @param container Netty container implementation.
     */
    JerseyHttp2ServerHandler(URI baseUri, NettyHttpContainer container) {
        this.baseUri = baseUri;
        this.container = container;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            onDataRead(ctx, (Http2DataFrame) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    /**
     * Process incoming data.
     */
    private void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) throws Exception {
        isList.add(new ByteBufInputStream(data.content()));
        if (data.isEndStream()) {
            isList.add(NettyInputStream.END_OF_INPUT);
        }
    }

    /**
     * Process incoming request (just a headers in this case, entity is processed separately).
     */
    private void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers) throws Exception {

        final ContainerRequest requestContext = createContainerRequest(ctx, headers);

        requestContext.setWriter(new NettyHttp2ResponseWriter(ctx, headers, container));

        // must be like this, since there is a blocking read from Jersey
        container.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                container.getApplicationHandler().handle(requestContext);
            }
        });
    }

    /**
     * Create Jersey {@link ContainerRequest} based on Netty {@link HttpRequest}.
     *
     * @param ctx          Netty channel context.
     * @param http2Headers Netty Http/2 headers.
     * @return created Jersey Container Request.
     */
    private ContainerRequest createContainerRequest(ChannelHandlerContext ctx, Http2HeadersFrame http2Headers) {

        String path = http2Headers.headers().path().toString();

        String s = path.startsWith("/") ? path.substring(1) : path;
        URI requestUri = URI.create(baseUri + ContainerUtils.encodeUnsafeCharacters(s));

        ContainerRequest requestContext = new ContainerRequest(
                baseUri, requestUri, http2Headers.headers().method().toString(), getSecurityContext(),
                new PropertiesDelegate() {

                    private final Map<String, Object> properties = new HashMap<>();

                    @Override
                    public Object getProperty(String name) {
                        return properties.get(name);
                    }

                    @Override
                    public Collection<String> getPropertyNames() {
                        return properties.keySet();
                    }

                    @Override
                    public void setProperty(String name, Object object) {
                        properties.put(name, object);
                    }

                    @Override
                    public void removeProperty(String name) {
                        properties.remove(name);
                    }
                });

        // request entity handling.
        if (!http2Headers.isEndStream()) {

            ctx.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    isList.add(NettyInputStream.END_OF_INPUT_ERROR);
                }
            });

            requestContext.setEntityStream(new NettyInputStream(isList));
        } else {
            requestContext.setEntityStream(new InputStream() {
                @Override
                public int read() throws IOException {
                    return -1;
                }
            });
        }

        // copying headers from netty request to jersey container request context.
        for (CharSequence name : http2Headers.headers().names()) {
            requestContext.headers(name.toString(), mapToString(http2Headers.headers().getAll(name)));
        }

        return requestContext;
    }

    private List<String> mapToString(List<CharSequence> list) {
        ArrayList<String> result = new ArrayList<>(list.size());

        for (CharSequence sequence : list) {
            result.add(sequence.toString());
        }

        return result;
    }

    private SecurityContext getSecurityContext() {
        return new SecurityContext() {

            @Override
            public boolean isUserInRole(final String role) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        };
    }
}
