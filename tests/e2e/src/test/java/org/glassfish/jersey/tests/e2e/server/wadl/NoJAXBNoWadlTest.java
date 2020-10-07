/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.wadl;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.util.JdkVersion;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class NoJAXBNoWadlTest extends JerseyTest {

    private static PrintStream errorStream;
    private static OutputStream readableStream = new ByteArrayOutputStream(800);

    @BeforeClass
    public static void before() {
        errorStream = System.err;
        System.setErr(new PrintStream(readableStream));
    }

    @AfterClass
    public static void after() {
        System.setErr(errorStream);
    }

    @Path("dummy")
    public static class NoJAXBNoWadlDummyResource {
        @PUT
        public String put(String put) {
            return "OK";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(NoJAXBNoWadlDummyResource.class);
    }

    @Test
    public void testOptionsNoWadl() {
        // Make sure the test does not have JAX-B on a classpath
        Assert.assertFalse(ServiceFinder.find("jakarta.xml.bind.JAXBContext").iterator().hasNext());

        try (Response r = target("dummy").request(MediaTypes.WADL_TYPE).options()) {
            String headers = r.getHeaderString(HttpHeaders.ALLOW);
            Assert.assertEquals("OPTIONS,PUT", headers);
        }
        System.out.println(readableStream.toString());
        Assert.assertTrue(readableStream.toString().contains(LocalizationMessages.WADL_FEATURE_DISABLED()));
    }
}
