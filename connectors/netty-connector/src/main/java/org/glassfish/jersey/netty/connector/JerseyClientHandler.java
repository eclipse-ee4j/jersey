/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.netty.connector.internal.NettyInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Jersey implementation of Netty channel handler.
 *
 * @author Pavel Bucek
 */
class JerseyClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final ClientRequest jerseyRequest;
    private final CompletableFuture<ClientResponse> responseAvailable;
    private final CompletableFuture<?> responseDone;

    private NettyInputStream nis;
    private ClientResponse jerseyResponse;

    private boolean readTimedOut;

    JerseyClientHandler(ClientRequest request,
                        CompletableFuture<ClientResponse> responseAvailable,
                        CompletableFuture<?> responseDone) {
        this.jerseyRequest = request;
        this.responseAvailable = responseAvailable;
        this.responseDone = responseDone;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
       notifyResponse();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
       // assert: no-op, if channel is closed after LastHttpContent has been consumed

       if (readTimedOut) {
          responseDone.completeExceptionally(new TimeoutException("Stream closed: read timeout"));
       } else {
          responseDone.completeExceptionally(new IOException("Stream closed"));
       }
    }

    protected void notifyResponse() {
       if (jerseyResponse != null) {
          ClientResponse cr = jerseyResponse;
          jerseyResponse = null;
          responseAvailable.complete(cr);
       }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            final HttpResponse response = (HttpResponse) msg;

            jerseyResponse = new ClientResponse(new Response.StatusType() {
                @Override
                public int getStatusCode() {
                    return response.status().code();
                }

                @Override
                public Response.Status.Family getFamily() {
                    return Response.Status.Family.familyOf(response.status().code());
                }

                @Override
                public String getReasonPhrase() {
                    return response.status().reasonPhrase();
                }
            }, jerseyRequest);

            for (Map.Entry<String, String> entry : response.headers().entries()) {
                jerseyResponse.getHeaders().add(entry.getKey(), entry.getValue());
            }

            // request entity handling.
            if ((response.headers().contains(HttpHeaderNames.CONTENT_LENGTH) && HttpUtil.getContentLength(response) > 0)
                    || HttpUtil.isTransferEncodingChunked(response)) {

                nis = new NettyInputStream();
                responseDone.whenComplete((_r, th) -> nis.complete(th));

                jerseyResponse.setEntityStream(nis);
            } else {
                jerseyResponse.setEntityStream(new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return -1;
                    }
                });
            }
        }
        if (msg instanceof HttpContent) {

            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();

            if (content.isReadable()) {
                content.retain();
                nis.publish(content);
            }

            if (msg instanceof LastHttpContent) {
                responseDone.complete(null);
                notifyResponse();
            }
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final Throwable cause) {
        responseDone.completeExceptionally(cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
       if (evt instanceof IdleStateEvent) {
          readTimedOut = true;
          ctx.close();
       } else {
           super.userEventTriggered(ctx, evt);
       }
    }
}
