/*
 * Copyright (c) 2023, 2025 Oracle and/or its affiliates. All rights reserved.
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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class JerseyExpectContinueHandler extends ChannelInboundHandlerAdapter {

    private ExpectationState currentState = ExpectationState.IDLE;

    private static final List<HttpResponseStatus> finalErrorStatuses = Arrays.asList(HttpResponseStatus.UNAUTHORIZED,
            HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
    private static final List<HttpResponseStatus> reSendErrorStatuses = Arrays.asList(
            HttpResponseStatus.METHOD_NOT_ALLOWED,
            HttpResponseStatus.EXPECTATION_FAILED);

    private static final List<HttpResponseStatus> errorStatuses = new ArrayList<>(finalErrorStatuses);
    private static final List<HttpResponseStatus> statusesToBeConsidered = new ArrayList<>(reSendErrorStatuses);

    static {
        errorStatuses.addAll(reSendErrorStatuses);
        statusesToBeConsidered.addAll(finalErrorStatuses);
        statusesToBeConsidered.add(HttpResponseStatus.CONTINUE);
    }

    private HttpResponseStatus status = null;

    private CountDownLatch latch = null;

    private boolean propagateLastMessage = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (checkExpectResponse(msg)) {
            currentState = ExpectationState.AWAITING;
        }
        switch (currentState) {
            case AWAITING:
                final HttpResponse response = (HttpResponse) msg;
                status = response.status();

                boolean handshakeDone = processErrorStatuses(status, ctx) || msg instanceof FullHttpMessage;
                currentState = (handshakeDone) ? ExpectationState.IDLE : ExpectationState.FINISHING;
                processLatch();
                return;
            case FINISHING:
                if (msg instanceof LastHttpContent) {
                    currentState = ExpectationState.IDLE;
                    if (propagateLastMessage) {
                        propagateLastMessage = false;
                        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    }
                }
                return;
            default:
                ctx.fireChannelRead(msg); //bypass the message to the next handler in line
        }
    }

    private boolean checkExpectResponse(Object msg) {
        if (currentState == ExpectationState.IDLE && latch != null && msg instanceof HttpResponse) {
            return statusesToBeConsidered.contains(((HttpResponse) msg).status());
        }
        return false;
    }

    boolean processErrorStatuses(HttpResponseStatus status, ChannelHandlerContext ctx)
            throws InterruptedException {
        if (reSendErrorStatuses.contains(status)) {
            propagateLastMessage = true;
        }
        return (finalErrorStatuses.contains(status));
    }

    boolean processExpectationStatus()
            throws TimeoutException, IOException {
        if (status == null) {
            throw new TimeoutException(); // continue without expectations
        }
        if (!statusesToBeConsidered.contains(status)) {
            throw new ProcessingException(LocalizationMessages
                    .UNEXPECTED_VALUE_FOR_EXPECT_100_CONTINUE_STATUSES(status.code()), null);
        }

        if (finalErrorStatuses.contains(status)) {
            throw new IOException(LocalizationMessages
                    .EXPECT_100_CONTINUE_FAILED_REQUEST_FAILED(), null);
        }

        if (reSendErrorStatuses.contains(status)) {
            throw new TimeoutException(LocalizationMessages
                    .EXPECT_100_CONTINUE_FAILED_REQUEST_SHOULD_BE_RESENT()); // Re-send request without expectations
        }

        return true;
    }

    void resetHandler() {
        latch = null;
    }

    void attachCountDownLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    private void processLatch() {
        if (latch != null) {
            latch.countDown();
        }
    }

    private enum ExpectationState {
        AWAITING,
        FINISHING,
        IDLE
    }
}
