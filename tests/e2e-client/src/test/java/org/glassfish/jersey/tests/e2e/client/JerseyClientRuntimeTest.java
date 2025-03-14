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

package org.glassfish.jersey.tests.e2e.client;

import org.glassfish.jersey.client.ClientConfig;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JerseyClientRuntimeTest {

    private static int COUNT = 10;
    private List<WeakReference<Object>> list = new ArrayList<>();
    private ReferenceQueue queue = new ReferenceQueue();

    @Test
    public void testClientRuntimeInstancesAreGCed() throws InterruptedException {
        Client c = ClientBuilder.newClient();
        c.register(new ClientRequestFilter() {
            @Override
            public void filter(ClientRequestContext requestContext) throws IOException {
                requestContext.abortWith(Response
                                .ok("<myDTO xmlns=\"http://org.example.dtos\"/>")
                                .type(MediaType.APPLICATION_XML_TYPE)
                        .build());
            }
        });

        WebTarget target = c.target("http://localhost/nowhere");
        for (int i = 0; i != COUNT; i++) {
            target = target.property("SOME", "PROPERTY");
            ClientConfig config = (ClientConfig) target.getConfiguration();
            Object clientRuntime = getClientRuntime(config);
            addToList(clientRuntime);
            try (Response response = target.request().get()) {
                MatcherAssert.assertThat(response.getStatus(), Matchers.is(200));
                MyDTO dto = response.readEntity(MyDTO.class);
                MatcherAssert.assertThat(dto, Matchers.notNullValue());
            }
        }

        System.gc();
        do {
            Thread.sleep(100L);
        } while (queueIsEmpty(queue));

        c.close();

    }

    private static Object getClientRuntime(ClientConfig config) {
        try {
            Method m = ClientConfig.class.getDeclaredMethod("getRuntime");
            m.setAccessible(true);
            return m.invoke(config);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean queueIsEmpty(ReferenceQueue queue) {
        return queue.poll() == null;
    }

    private void addToList(Object object) {
        list.add(new WeakReference<>(object, queue));
    }

    @XmlRootElement(name = "myDTO", namespace = "http://org.example.dtos")
    public static class MyDTO {

    }
}
