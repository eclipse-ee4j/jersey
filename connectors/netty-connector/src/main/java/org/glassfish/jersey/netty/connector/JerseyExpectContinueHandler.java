/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import org.glassfish.jersey.client.ClientRequest;

import jakarta.ws.rs.ProcessingException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JerseyExpectContinueHandler extends ChannelInboundHandlerAdapter {

    private boolean isExpected;

    private static final List<HttpResponseStatus> statusesToBeConsidered = Arrays.asList(HttpResponseStatus.CONTINUE,
            HttpResponseStatus.UNAUTHORIZED, HttpResponseStatus.EXPECTATION_FAILED,
            HttpResponseStatus.METHOD_NOT_ALLOWED, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);

    private CompletableFuture<HttpResponseStatus> expectedFuture = new CompletableFuture<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isExpected && msg instanceof HttpResponse) {
            final HttpResponse response = (HttpResponse) msg;
            if (statusesToBeConsidered.contains(response.status())) {
                expectedFuture.complete(response.status());
            }
            if (!HttpResponseStatus.CONTINUE.equals(response.status())) {
                ctx.fireChannelRead(msg); //bypass the message to the next handler in line
            } else {
                ctx.pipeline().remove(JerseyExpectContinueHandler.class);
            }
        } else {
            if (!isExpected
                    && ctx.pipeline().context(JerseyExpectContinueHandler.class) != null) {
                ctx.pipeline().remove(JerseyExpectContinueHandler.class);
            }
            ctx.fireChannelRead(msg); //bypass the message to the next handler in line
        }
    }

    CompletableFuture<HttpResponseStatus> processExpect100ContinueRequest(HttpRequest nettyRequest,
                                                                          ClientRequest jerseyRequest,
                                                                          Channel ch,
                                                                          Integer timeout)
            throws InterruptedException, ExecutionException, TimeoutException {
        //check for 100-Continue presence/availability
        final Expect100ContinueConnectorExtension expect100ContinueExtension
                = new Expect100ContinueConnectorExtension();

        final DefaultFullHttpRequest nettyRequestHeaders =
                new DefaultFullHttpRequest(nettyRequest.protocolVersion(), nettyRequest.method(), nettyRequest.uri());
        nettyRequestHeaders.headers().setAll(nettyRequest.headers());

        if (!nettyRequestHeaders.headers().contains(HttpHeaderNames.HOST)) {
            nettyRequestHeaders.headers().add(HttpHeaderNames.HOST, jerseyRequest.getUri().getHost());
        }

        //If Expect:100-continue feature is enabled and client supports it, the nettyRequestHeaders will be
        //enriched with the 'Expect:100-continue' header.
        expect100ContinueExtension.invoke(jerseyRequest, nettyRequestHeaders);

        final ChannelFuture expect100ContinueFuture = (HttpUtil.is100ContinueExpected(nettyRequestHeaders))
                // Send only head of the HTTP request enriched with Expect:100-continue header.
                ? ch.writeAndFlush(nettyRequestHeaders)
                // Expect:100-Continue either is not supported or is turned off
                : null;
        isExpected = expect100ContinueFuture != null;
        if (!isExpected) {
            ch.pipeline().remove(JerseyExpectContinueHandler.class);
        } else {
            final HttpResponseStatus status = expectedFuture
                    .get(timeout, TimeUnit.MILLISECONDS);

            processExpectationStatus(status);
        }
        return expectedFuture;
    }

    private void processExpectationStatus(HttpResponseStatus status)
            throws TimeoutException {
        if (!statusesToBeConsidered.contains(status)) {
            throw new ProcessingException(LocalizationMessages
                    .UNEXPECTED_VALUE_FOR_EXPECT_100_CONTINUE_STATUSES(status.code()), null);
        }
        if (!expectedFuture.isDone() || HttpResponseStatus.EXPECTATION_FAILED.equals(status)) {
            isExpected = false;
            throw new TimeoutException(); // continue without expectations
        }
        if (!HttpResponseStatus.CONTINUE.equals(status)) {
            throw new ProcessingException(LocalizationMessages
                    .UNEXPECTED_VALUE_FOR_EXPECT_100_CONTINUE_STATUSES(status.code()), null);
        }
    }

    boolean isExpected() {
        return isExpected;
    }
}