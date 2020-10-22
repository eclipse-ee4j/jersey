/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.internal;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.RequestEntityProcessing;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

class HttpUrlExpect100ContinueConnectorExtension
        implements ConnectorExtension<HttpURLConnection, IOException> {

    private static final String EXCEPTION_MESSAGE = "Server rejected operation";

    @Override
    public  void invoke(ClientRequest request, HttpURLConnection uc) {

        final long length = request.getLengthLong();
        final RequestEntityProcessing entityProcessing = request.resolveProperty(
                ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.class);

        final Boolean expectContinueActivated = request.resolveProperty(
                ClientProperties.EXPECT_100_CONTINUE, Boolean.class);
        final Long expectContinueSizeThreshold = request.resolveProperty(
                ClientProperties.EXPECT_100_CONTINUE_THRESHOLD_SIZE,
                ClientProperties.DEFAULT_EXPECT_100_CONTINUE_THRESHOLD_SIZE);

        final boolean allowStreaming = length > expectContinueSizeThreshold
                || entityProcessing == RequestEntityProcessing.CHUNKED;

        if (!Boolean.TRUE.equals(expectContinueActivated)
                || !("POST".equals(uc.getRequestMethod()) || "PUT".equals(uc.getRequestMethod()))
                || !allowStreaming
        ) {
            return;
        }
        uc.setRequestProperty("Expect", "100-Continue");
    }

    @Override
    public void postConnectionProcessing(HttpURLConnection extensionParam) {
        //nothing here, we do not process post connection extension
    }

    @Override
    public boolean handleException(ClientRequest request, HttpURLConnection extensionParam, IOException ex) {

        final Boolean expectContinueActivated = request.resolveProperty(
                ClientProperties.EXPECT_100_CONTINUE, Boolean.FALSE);

        return expectContinueActivated
                && (ex instanceof ProtocolException && ex.getMessage().equals(EXCEPTION_MESSAGE));
    }

}