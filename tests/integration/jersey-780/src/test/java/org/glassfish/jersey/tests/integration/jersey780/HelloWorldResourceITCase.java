/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey780;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author Michal Gajdos
 */
public class HelloWorldResourceITCase extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig().registerInstances(new Jersey780());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testInvalidUrl() throws Exception {
        List<Integer> expectedCodes = Arrays.asList(
                Response.Status.BAD_REQUEST.getStatusCode(), Response.Status.NOT_FOUND.getStatusCode());
        List<String> expectedPhrases = Arrays.asList(
                Response.Status.BAD_REQUEST.getReasonPhrase(), Response.Status.NOT_FOUND.getReasonPhrase());

        final URL url = new URL(getBaseUri().toString() + "^");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.connect();

        final int statusCode = connection.getResponseCode();
        final String statusMessage = connection.getResponseMessage();

        connection.disconnect();

        assertTrue("Wrong response status code: " + statusCode,
                expectedCodes.contains(statusCode));
        assertTrue("Wrong response status reason: " + statusMessage,
                expectedPhrases.contains(statusMessage));
    }
}
