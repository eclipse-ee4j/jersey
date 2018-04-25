/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * Tests the servlet3-webapp example.
 * Integration test launched by maven-jetty-plugin
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class Servlet3WebappITCase extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new Application(); // dummy Application instance for test framework
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("animals").build();
    }

    @Test
    public void testClientStringResponse() {
        String s = target().path("dog").request().get(String.class);
        assertEquals("Woof!", s);

        s = target().path("cat").request().get(String.class);
        assertEquals("Miaow!", s);
    }
}
