/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlettests;

import java.net.HttpURLConnection;
import java.net.URL;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Test class related to issue JERSEY-2680.
 *
 * @author Michal Gajdos
 */
public class InvalidRequestUriITCase extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testInvalidRequestUriFilter() throws Exception {
        invalidRequestUri("filter");
    }

    @Test
    public void testInvalidRequestUriServlet() throws Exception {
        invalidRequestUri("servlet");
    }

    public void invalidRequestUri(final String path) throws Exception {
        final URL url = new URL(getBaseUri().toString() + path + "/resource{");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/plain");
        connection.connect();

        assertEquals(400, connection.getResponseCode());
    }
}
