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

package org.glassfish.jersey.tests.integration.jersey2654;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;

import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Reproducer for JERSEY-2654
 *
 * Tests, that unencoded curly brackets (typically used in URI queries containing JSON) do not cause the request to
 * fail when running in a servlet environment and configured as a filter.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class Jersey2654ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testJsonInUriWithSockets() throws IOException {
        // Low level approach with sockets is used, because common Java HTTP clients are using java.net.URI,
        // which fails when unencoded curly bracket is part of the URI
        final Socket socket = new Socket(getBaseUri().getHost(), getBaseUri().getPort());
        final PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

        // quotes are encoded by browsers, curly brackets are not, so the quotes will be sent pre-encoded
        // HTTP 1.0 is used for simplicity
        pw.println("GET /filter?json={%22foo%22:%22bar%22} HTTP/1.0");
        pw.println();   // http request should end with a blank line
        pw.flush();

        final BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String lastLine = null;
        String line;
        while ((line = br.readLine()) != null) {
            // read the response and remember the last line
            lastLine = line;
        }
        assertEquals("{\"foo\":\"bar\"}", lastLine);
        br.close();
    }
}
