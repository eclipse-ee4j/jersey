/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey5796;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientLifecycleListener;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;


public class Jersey5796Test extends JerseyTest {

    private static final int COUNT = 50;

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Test
    public void testMemoryLeak() throws Exception {
        ClientRuntimeCloseVerifier.closedClientRuntime = new AtomicInteger(0);
        Client client = ClientBuilder.newClient(new ClientConfig(ClientRuntimeCloseVerifier.class));
        assertEquals(0, ClientRuntimeCloseVerifier.closedClientRuntime.get());
        for (int i = 0; i < COUNT; i++) {
            Response response = client.target(getBaseUri()).property("test", "test").path("/get1").request().get();
            assertEquals("GET", response.readEntity(String.class));
            response.close();
        }
        System.gc();
        do {
            Thread.sleep(100L);
        } while (ClientRuntimeCloseVerifier.closedClientRuntime.get() != 50);
        assertEquals(COUNT, ClientRuntimeCloseVerifier.closedClientRuntime.get());
        client.close();

    }

    /* Reproduces issue 4507
        MultiException stack 1 of 1
        java.lang.IllegalStateException: ServiceLocatorImpl(__HK2_Generated_0,0,427183206) has been shut down
            at org.jvnet.hk2.internal.ServiceLocatorImpl.checkState(ServiceLocatorImpl.java:2399)
            at org.jvnet.hk2.internal.ServiceLocatorImpl.getServiceHandleImpl(ServiceLocatorImpl.java:627)
            at org.jvnet.hk2.internal.ServiceLocatorImpl.getServiceHandle(ServiceLocatorImpl.java:620)
            at org.jvnet.hk2.internal.ServiceLocatorImpl.getServiceHandle(ServiceLocatorImpl.java:638)
            at org.jvnet.hk2.internal.FactoryCreator.getFactoryHandle(FactoryCreator.java:79)
            at org.jvnet.hk2.internal.FactoryCreator.dispose(FactoryCreator.java:149)
            at org.jvnet.hk2.internal.SystemDescriptor.dispose(SystemDescriptor.java:521)
            at org.glassfish.jersey.inject.hk2.RequestContext.lambda$findOrCreate$0(RequestContext.java:60)
            at org.glassfish.jersey.internal.inject.ForeignDescriptorImpl.dispose(ForeignDescriptorImpl.java:63)
            at org.glassfish.jersey.inject.hk2.Hk2RequestScope$Instance.remove(Hk2RequestScope.java:126)
            at java.base/java.lang.Iterable.forEach(Iterable.java:75)
            at org.glassfish.jersey.inject.hk2.Hk2RequestScope$Instance.release(Hk2RequestScope.java:143)
            at org.glassfish.jersey.server.ChunkedOutput.flushQueue(ChunkedOutput.java:405)
            at org.glassfish.jersey.server.ChunkedOutput.write(ChunkedOutput.java:264)
            at org.glassfish.jersey.tests.integration.jersey5796.Jersey5796Test$Resource.lambda$get2$0(Jersey5796Test.java:116)
            at java.base/java.lang.Thread.run(Thread.java:1583)

     */
    @Test
    public void testChunkedInput() throws Exception {
        ClientRuntimeCloseVerifier.closedClientRuntime = new AtomicInteger(0);
        Client client = ClientBuilder.newClient(new ClientConfig(ClientRuntimeCloseVerifier.class));
        assertEquals(0, ClientRuntimeCloseVerifier.closedClientRuntime.get());
        for (int i = 0; i < COUNT; i++) {
            ChunkedInput<String> chunkedInput = client.target(getBaseUri()).property("test", "test")
                    .path("/get2").request().get(new GenericType<ChunkedInput<String>>() {});
            chunkedInput.setParser(ChunkedInput.createParser("\n"));
            int j = 0;
            String chunk;
            while ((chunk = chunkedInput.read()) != null) {
                assertEquals("Chunk " + j, chunk);
                j++;
            }
            chunkedInput.close();
        }
        System.gc();
        do {
            Thread.sleep(100L);
        } while (ClientRuntimeCloseVerifier.closedClientRuntime.get() != 50);
        assertEquals(COUNT, ClientRuntimeCloseVerifier.closedClientRuntime.get());
        client.close();
    }

    @Path("/")
    public static class Resource {

        @GET
        @Path("/get1")
        public String get1() {
            return "GET";
        }

        @GET
        @Path("/get2")
        public ChunkedOutput<String> get2() {
            ChunkedOutput<String> output = new ChunkedOutput<>(String.class);
            new Thread(() -> {
                try {
                    for (int i = 0; i < 3; i++) {
                        output.write("Chunk " + i + "\n");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        output.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }).start();
            return output;
        }
    }

    public static class ClientRuntimeCloseVerifier implements ClientLifecycleListener {

        private static AtomicInteger closedClientRuntime;

        @Override
        public void onInit() {
        }

        @Override
        public void onClose() {
            closedClientRuntime.incrementAndGet();
        }
    }
}