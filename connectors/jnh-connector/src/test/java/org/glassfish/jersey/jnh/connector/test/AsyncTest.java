/*
 * Copyright (c) 2021, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector.test;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

/**
 * Tests asynchronous and interleaved requests.
 */
public class AsyncTest extends AbstractJavaConnectorTest {
    /**
     * Checks, that 3 interleaved requests all complete and return their associated responses.
     * Additionally, checks, that all requests complete in 3 times the running time on the server.
     */
    @Test
    public void testAsyncRequestsWithoutTimeout() {
        Future<Response> request1 = this.requestAsyncWithEntity("java-connector/async", "POST", Entity.text("request1"));
        Future<Response> request2 = this.requestAsyncWithEntity("java-connector/async", "POST", Entity.text("request2"));
        Future<Response> request3 = this.requestAsyncWithEntity("java-connector/async", "POST", Entity.text("request3"));

        assertThatCode(() -> {
            // wait 3 times the processing time and throw if not completed until then
            await().atMost(3 * 3000, TimeUnit.MILLISECONDS)
                    .until(() -> request1.isDone() && request2.isDone() && request3.isDone());
            String response1 = request1.get().readEntity(String.class);
            String response2 = request2.get().readEntity(String.class);
            String response3 = request3.get().readEntity(String.class);
            assertThat(response1).isEqualTo("request1");
            assertThat(response2).isEqualTo("request2");
            assertThat(response3).isEqualTo("request3");
        }).doesNotThrowAnyException();
    }

    /**
     * Checks, that a status {@link Response.Status#SERVICE_UNAVAILABLE} is thrown, if a request computes too long.
     */
    @Test
    public void testAsyncRequestsWithTimeout() throws ExecutionException, InterruptedException {
        try {
            Response response = target().path("java-connector").path("async").queryParam("timeout", 1)
                    .request().async().post(Entity.text("")).get();
            assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
            assertThat(response.readEntity(String.class)).isEqualTo("Timeout");
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException("Could not correctly get response", ex);
        }
    }
}
