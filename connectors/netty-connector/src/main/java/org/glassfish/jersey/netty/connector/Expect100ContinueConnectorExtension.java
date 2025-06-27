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

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.internal.ConnectorExtension;

import java.io.IOException;
import java.net.ProtocolException;

class Expect100ContinueConnectorExtension
        implements ConnectorExtension<HttpRequest, IOException> {

    private final NettyConnectorProvider.Config.RW requestConfiguration;

    Expect100ContinueConnectorExtension(NettyConnectorProvider.Config.RW requestConfiguration) {
        this.requestConfiguration = requestConfiguration;
    }

    private static final String EXCEPTION_MESSAGE = "Server rejected operation";
    @Override
    public void invoke(ClientRequest request, HttpRequest extensionParam) {

        final long length = request.getLengthLong();
        final RequestEntityProcessing entityProcessing = requestConfiguration.requestEntityProcessing(request);
        final Boolean expectContinueActivated = requestConfiguration.expect100Continue(request);
        final long expectContinueSizeThreshold = requestConfiguration.expect100ContinueThreshold(request);
        final boolean allowStreaming = length > expectContinueSizeThreshold
                || entityProcessing == RequestEntityProcessing.CHUNKED;

        if (extensionParam.protocolVersion().equals(HttpVersion.HTTP_1_0)
                || !Boolean.TRUE.equals(expectContinueActivated)
                || !request.hasEntity()
                || !allowStreaming) {
            return;
        }
        extensionParam.headers().add(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);

    }

    @Override
    public void postConnectionProcessing(HttpRequest extensionParam) {
    }

    @Override
    public boolean handleException(ClientRequest request, HttpRequest extensionParam, IOException ex) {
        final Boolean expectContinueActivated = request.resolveProperty(
                ClientProperties.EXPECT_100_CONTINUE, Boolean.FALSE);

        return expectContinueActivated
                && (ex instanceof ProtocolException && ex.getMessage().equals(EXCEPTION_MESSAGE));
    }
}