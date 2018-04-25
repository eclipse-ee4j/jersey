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

package org.glassfish.jersey.tests.e2e.container;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests Jetty getHeader() - returns null in when the header
 * is not present and even though the header is empty string.
 *
 * ISSUE: JERSEY-2917
 *
 * @author Petr Bouda
 * */
public class JettyEmptyHeaderParamTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(HeaderResource.class);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new JettyTestContainerFactory();
    }

    @Path("test")
    public static class HeaderResource {

        @GET
        public String get(@HeaderParam("a") String value) {
            return value;
        }

    }

    @Test
    public void testNullHeader() throws Exception {
        String response = target("test").request().get(String.class);
        assertEquals("", response);
    }

    @Test
    public void testEmptyHeader() throws Exception {
        String response = target("test").request().header("a", "").get(String.class);
        assertEquals("", response);
    }
}
