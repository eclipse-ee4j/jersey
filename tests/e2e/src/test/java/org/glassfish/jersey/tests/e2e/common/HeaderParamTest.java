/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Miroslav Fuksa
 */
public class HeaderParamTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class, LoggingFeature.class);
    }

    @Path("resource")
    public static class MyResource {

        @GET
        public String get(@HeaderParam("hello") List<String> headers) {
            return headers.size() + ":" + headers;
        }
    }

    @Test
    public void testHeaderListSingleHeader() throws Exception {
        Response response = target().path("resource").request().header("hello", "world").header("hello", "universe").get();

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(String.class), equalTo("1:[world,universe]"));
    }

    /**
     * Check that multi value http headers are correctly read by the server.
     */
    @Test
    public void testHeaderListMultipleHeaders() throws Exception {
        final URL url = new URL(getBaseUri().toString() + "resource");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setRequestProperty("hello", "world");
        connection.addRequestProperty("hello", "universe");

        connection.setDoOutput(false);
        connection.connect();

        assertThat(connection.getResponseCode(), equalTo(200));
        assertThat(ReaderWriter.readFromAsString(new InputStreamReader(connection.getInputStream())),
                equalTo("2:[world, universe]"));
    }
}
