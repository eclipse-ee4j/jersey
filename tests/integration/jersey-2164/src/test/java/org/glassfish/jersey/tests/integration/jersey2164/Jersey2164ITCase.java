/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2164;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Reproducer tests for JERSEY-2164.
 *
 * @author Michal Gajdos
 */
public class Jersey2164ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new Jersey2164();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testHeaderListSingleHeader() throws Exception {
        Response response = target().request().header("hello", "world").header("hello", "universe").get();

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(String.class), equalTo("1:[world,universe]"));
    }

    /**
     * Check that multi value http headers are correctly read by the server.
     */
    @Test
    public void testHeaderListMultipleHeaders() throws Exception {
        final URL url = new URL(getBaseUri().toString());
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
