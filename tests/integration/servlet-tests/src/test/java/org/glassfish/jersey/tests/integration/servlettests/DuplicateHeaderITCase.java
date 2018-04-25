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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class DuplicateHeaderITCase extends JerseyTest {
    @Override
    protected Application configure() {
        // dummy resource config
        return new ResourceConfig();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testDuplicateHeader() throws IOException {
        testDuplicateHeaderImpl("contextPathFilter/contextPathResource");
        testDuplicateHeaderImpl("servlet/contextPathResource");
    }

    private void testDuplicateHeaderImpl(final String path) throws IOException {
        testDuplicateHeaderImpl(0, HttpURLConnection.HTTP_OK, path);
        testDuplicateHeaderImpl(1, HttpURLConnection.HTTP_OK, path);
        testDuplicateHeaderImpl(2, HttpURLConnection.HTTP_BAD_REQUEST, path);
    }

    private void testDuplicateHeaderImpl(final int headerCount, int expectedResponseCode, final String path)
            throws IOException {
        final String headerName = HttpHeaders.CONTENT_TYPE;
        URL getUrl = UriBuilder.fromUri(getBaseUri()).path(path).build().toURL();
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        try {
            connection.setRequestMethod("GET");
            for (int i = 0; i < headerCount; i++) {
                connection.addRequestProperty(headerName, "N/A");
            }
            connection.connect();
            assertEquals(path + " [" + headerName + ":" + headerCount + "x]", expectedResponseCode, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

}
