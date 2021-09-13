/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MultivaluedHashMap;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;


public class NettyConnectorTest extends JerseyTest {

    private static final String PATH = "test";
    private final Map<String, Object> properties = new HashMap<>();
    private URI uri;
    @Mock
    private Client client;
    @Mock
    private ClientRequest jerseyRequest;
    @Mock
    private Configuration configuration;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
        uri = URI.create(getBaseUri().toString() + "test");
        when(jerseyRequest.getUri()).thenReturn(uri);
        when(jerseyRequest.getConfiguration()).thenReturn(configuration);
        when(jerseyRequest.resolveProperty(anyString(), anyInt())).thenReturn(0);
        when(jerseyRequest.getMethod()).thenReturn("GET");
        when(jerseyRequest.getStringHeaders()).thenReturn(new MultivaluedHashMap<>());
        when(client.getConfiguration()).thenReturn(configuration);
        when(configuration.getProperties()).thenReturn(properties);
    }

    // This test is for debugging purposes. By default it works with and without the fix.
    @Test
    public void issue4851() {
        try {
            NettyConnector connector = new NettyConnector(client);
            ClientResponse response = connector.apply(jerseyRequest);
            assertEquals(200, response.getStatus());
            checkPipeline(connector, true, NettyConnector.INACTIVE_POOLED_CONNECTION_HANDLER);
            Future<?> future = connector.apply(jerseyRequest, new AsyncConnectorCallbackImpl());
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void checkPipeline(NettyConnector connector, boolean expected, String name) {
        String key = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
        ArrayList<Channel> channels = connector.connections.get(key);
        assertEquals(1, channels.size());
        Channel channel = channels.get(0);
        ChannelPipeline pipeline = channel.pipeline();
        Set<String> names = new HashSet<>();
        for (Entry<String, ChannelHandler> entry : pipeline) {
            names.add(entry.getKey());
        }
        assertEquals(expected, names.contains(name));
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class);
    }

    @Path(PATH)
    public static class MyResource {
        @GET
        public String get() throws InterruptedException {
            return "ok";
        }
    }

    private static class AsyncConnectorCallbackImpl implements AsyncConnectorCallback {
        @Override
        public void response(ClientResponse response) {}
        @Override
        public void failure(Throwable failure) {}
    }
}
