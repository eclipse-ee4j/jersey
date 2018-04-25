/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;

/**
 * Test if URI can contain unsafe characters in the query parameter, e.g. for sending JSON
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class UnsafeCharsInUriTest extends JerseyTest {
    @Override
    protected ResourceConfig configure() {
        ResourceConfig rc = new ResourceConfig(UnsafeCharsInUriTest.ResponseTest.class);
        return rc;
    }

    /**
     * Test resource
     */
    @Path(value = "/app")
    public static class ResponseTest {
        /**
         * Test resource method returning the content of the {@code msg} query parameter.
         *
         * @return the {@code msg} query parameter (as received)
         */
        @GET
        @Path("test")
        public Response jsonQueryParamTest(@DefaultValue("") @QueryParam("msg") final String msg) {
            return Response.ok().entity(msg).build();
        }

    }

    /**
     * Test, that server can consume JSON (curly brackets) and other unsafe characters sent in the query parameter
     *
     * @throws IOException
     */
    @Test
    public void testSpecCharsInUriWithSockets() throws IOException {
        // quotes are encoded by browsers, curly brackets are not, so the quotes will be sent pre-encoded
        // HTTP 1.0 is used for simplicity
        String response = sendGetRequestOverSocket(getBaseUri(), "GET /app/test?msg={%22foo%22:%22bar%22} HTTP/1.0");
        assertArrayEquals("{\"foo\":\"bar\"}".getBytes(Charset.forName("ISO-8859-1")), response.getBytes());
    }

    @Test
    @Ignore("Incorrectly written test (doesn't deal with http encoding).")
    public void testSecialCharsInQueryParam() throws IOException {
        // quotes are encoded by browsers, curly brackets are not, so the quotes will be sent pre-encoded
        // HTTP 1.0 is used for simplicity
        String response = sendGetRequestOverSocket(getBaseUri(),
                                            "GET /app/test?msg=Hello\\World+With+SpecChars+§*)$!±@-_=;`:\\,~| HTTP/1.0");

        assertArrayEquals("Hello\\World With SpecChars §*)$!±@-_=;`:\\,~|".getBytes(Charset.forName("ISO-8859-1")),
                     response.getBytes());
    }

    private String sendGetRequestOverSocket(final URI baseUri, final String requestLine) throws IOException {
        // Low level approach with sockets is used, because common Java HTTP clients are using java.net.URI,
        // which fails when unencoded curly bracket is part of the URI
        final Socket socket = new Socket(baseUri.getHost(), baseUri.getPort());
        final PrintWriter pw =
                new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("ISO-8859-1"))));

        pw.println(requestLine);
        pw.println(); // http request should end with a blank line
        pw.flush();

        final BufferedReader br =
                new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));

        String lastLine = null;
        String line;
        while ((line = br.readLine()) != null) {
            // read the response and remember the last line
            lastLine = line;
        }
        pw.close();
        br.close();

        return lastLine;
    }
}




