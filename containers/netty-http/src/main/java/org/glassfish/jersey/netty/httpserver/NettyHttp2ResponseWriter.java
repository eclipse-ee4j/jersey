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
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;

/**
 * Netty implementation of {@link ContainerResponseWriter}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
class NettyHttp2ResponseWriter implements ContainerResponseWriter {

    private final ChannelHandlerContext ctx;
    private final Http2HeadersFrame headersFrame;
    private final NettyHttpContainer container;

    private volatile ScheduledFuture<?> suspendTimeoutFuture;
    private volatile Runnable suspendTimeoutHandler;

    NettyHttp2ResponseWriter(ChannelHandlerContext ctx, Http2HeadersFrame headersFrame, NettyHttpContainer container) {
        this.ctx = ctx;
        this.headersFrame = headersFrame;
        this.container = container;
    }

    @Override
    public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext)
            throws ContainerException {

        String reasonPhrase = responseContext.getStatusInfo().getReasonPhrase();
        int statusCode = responseContext.getStatus();

        HttpResponseStatus status = reasonPhrase == null
                ? HttpResponseStatus.valueOf(statusCode)
                : new HttpResponseStatus(statusCode, reasonPhrase);

        DefaultHttp2Headers response = new DefaultHttp2Headers();
        response.status(Integer.toString(responseContext.getStatus()));

        for (final Map.Entry<String, List<String>> e : responseContext.getStringHeaders().entrySet()) {
            response.add(e.getKey().toLowerCase(), e.getValue());
        }

        response.set(HttpHeaderNames.CONTENT_LENGTH, Long.toString(contentLength));

        ctx.writeAndFlush(new DefaultHttp2HeadersFrame(response));

        if (!headersFrame.headers().method().equals(HttpMethod.HEAD.asciiName())
            && (contentLength > 0 || contentLength == -1)) {

            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    write(new byte[]{(byte) b});
                }

                @Override
                public void write(byte[] b) throws IOException {
                    write(b, 0, b.length);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {

                    ByteBuf buffer = ctx.alloc().buffer(len);
                    buffer.writeBytes(b, off, len);

                    ctx.writeAndFlush(new DefaultHttp2DataFrame(buffer, false));
                }

                @Override
                public void flush() throws IOException {
                    ctx.flush();
                }

                @Override
                public void close() throws IOException {
                    ctx.write(new DefaultHttp2DataFrame(true)).addListener(NettyResponseWriter.FLUSH_FUTURE);
                }
            };

        } else {
            ctx.writeAndFlush(new DefaultHttp2DataFrame(true));
            return null;
        }
    }

    @Override
    public boolean suspend(long timeOut, TimeUnit timeUnit, final ContainerResponseWriter.TimeoutHandler
            timeoutHandler) {

        suspendTimeoutHandler = new Runnable() {
            @Override
            public void run() {
                timeoutHandler.onTimeout(NettyHttp2ResponseWriter.this);
            }
        };

        if (timeOut <= 0) {
            return true;
        }

        suspendTimeoutFuture =
                container.getScheduledExecutorService().schedule(suspendTimeoutHandler, timeOut, timeUnit);

        return true;
    }

    @Override
    public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) throws IllegalStateException {

        // suspend(0, .., ..) was called, so suspendTimeoutFuture is null.
        if (suspendTimeoutFuture != null) {
            suspendTimeoutFuture.cancel(true);
        }

        if (timeOut <= 0) {
            return;
        }

        suspendTimeoutFuture =
                container.getScheduledExecutorService().schedule(suspendTimeoutHandler, timeOut, timeUnit);
    }

    @Override
    public void commit() {
        ctx.flush();
    }

    @Override
    public void failure(Throwable error) {
        ctx.writeAndFlush(new DefaultHttp2Headers().status(HttpResponseStatus.INTERNAL_SERVER_ERROR.codeAsText()))
           .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public boolean enableResponseBuffering() {
        return true;
    }
}
