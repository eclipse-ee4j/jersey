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

package org.glassfish.jersey.jdk.connector.internal;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
@Ignore
public class PublicSitesTest extends JerseyTest {

    @Test
    public void testGoolgeCom() throws InterruptedException {
        doTest("https://www.google.com");
    }

    @Test
    public void testSeznam() throws InterruptedException {
        doTest("https://www.seznam.cz");
    }

    @Test
    public void testGoogleUK() throws InterruptedException {
        doTest("https://www.google.co.uk");
    }

    @Test
    public void testWikipedia() throws InterruptedException {
        doTest("http://www.wikipedia.com");
    }

    @Test
    public void testJavaNet() throws InterruptedException {
        doTest("http://www.java.net");
    }

    @Test
    public void testTheGuardian() throws InterruptedException {
        doTest("http://www.theguardian.com");
    }

    @Test
    public void testBbcUk() throws InterruptedException {
        doTest("http://www.bbc.co.uk");
    }

    @Test
    public void testServis24() throws InterruptedException {
        doTest("https://www.servis24.cz");
    }

    private void doTest(String url) {
        Response response = client().target(url).request().get();
        String htmlPage = response.readEntity(String.class);
        assertEquals(200, response.getStatus());
        assertTrue(htmlPage.contains("<html"));
        assertTrue(htmlPage.contains("</html>"));
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JdkConnectorProvider());
    }

    @Override
    protected Application configure() {
        return new ResourceConfig();
    }
}
