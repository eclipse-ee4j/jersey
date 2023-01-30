/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.grizzly2.httpserver.test.tools;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.glassfish.grizzly.http.util.Header;

import jakarta.ws.rs.core.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JDK11+ has it's own {@link HttpClient} implementation supporting both HTTP 1.1 and HTTP/2.
 *
 * @author David Matejcek
 */
public class JdkHttpClientThread extends ClientThread {
    private final HttpClient client;

    public JdkHttpClientThread(final ClientThreadSettings settings,
        final AtomicInteger counter, final AtomicReference<Throwable> error) throws Exception {
        super(settings, counter, error);
        this.client = createClient(settings.secured, settings.useHttp2);
    }


    @Override
    public void doGetAndCheckResponse() throws Throwable {
        final HttpRequest request = HttpRequest.newBuilder(getSettings().targetUri)
            .header(Header.ContentType.toString(), MediaType.TEXT_PLAIN).GET().build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("Got it!", response.body());
    }


    private static HttpClient createClient(final boolean secured, final boolean useHttp2) throws Exception {
        final HttpClient.Builder builder = HttpClient.newBuilder()
            .version(useHttp2 ? Version.HTTP_2 : Version.HTTP_1_1);
        if (secured) {
            builder.sslContext(createSslContext());
        }
        return builder.build();
    }
}
