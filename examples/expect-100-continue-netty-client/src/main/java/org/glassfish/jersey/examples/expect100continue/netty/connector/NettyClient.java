/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.expect100continue.netty.connector;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.client.http.Expect100ContinueFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
//        enableLogging(Level.FINE);
        test();
    }

    public static void test() throws InterruptedException {
        ClientConfig defaultConfig = new ClientConfig();
        defaultConfig.property(LoggingFeature.LOGGING_FEATURE_VERBOSITY_CLIENT, LoggingFeature.Verbosity.PAYLOAD_ANY);

        //The issue can be produced only by using NettyConnectorProvider
        defaultConfig.connectorProvider(new NettyConnectorProvider());

        //with below two lines, enabled 100-continue feature
        defaultConfig.property(ClientProperties.REQUEST_ENTITY_PROCESSING, RequestEntityProcessing.CHUNKED);
        defaultConfig.register(Expect100ContinueFeature.basic());

        Client client = ClientBuilder.newClient(defaultConfig);
        WebTarget webTarget = client.target("http://127.0.0.1:3000");
        Invocation.Builder invocationBuilder = webTarget.request();
        invocationBuilder.header("Accept", "application/json");

        for (int i = 0; i < 5; i++) { //iterating few times here to demonstrate
            // the 100-continue processing works on any iteration

            System.out.println();
            System.out.println("****************** Iteration #" + i + " ******************");

            final Response response = invocationBuilder.post(generateSimpleEntity());

            System.out.println("Response status = " + response.getStatus());
            System.out.println("Response status 204 means No Content, so we do not expect body here");
            System.out.println("**************************************************");
            System.out.println();
        }
        System.out.println("Client connection should be closed manually with Ctrl-C");
    }

    private static Entity<String> generateSimpleEntity(){
        return Entity.entity("{\"message\":\"Hello from java client\"}", MediaType.APPLICATION_JSON_TYPE);
    }

    private static void enableLogging(Level logLevel) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(logLevel);
        Logger nettyLog = Logger.getLogger("io.netty");
        nettyLog.setLevel(logLevel);
        Handler[] handlers = rootLogger.getHandlers();
        for (final Handler handler : handlers) {
            handler.setLevel(logLevel);
        }
    }
}
