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

package org.glassfish.jersey.tests.integration.servlet_3_init_8;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

/**
 * Test unreachable resources.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@RunWith(Parameterized.class)
public class HelloWorldResourceUnreachableITCase extends JerseyTest {

    private final String appPath;
    private final String resourcePath;

    public HelloWorldResourceUnreachableITCase(String appPath,
                                               String resourcePath) {
        this.appPath = appPath;
        this.resourcePath = resourcePath;
    }

    @Parameterized.Parameters(name = "{index}: {0}/{1}")
    public static List<Object[]> testData() {
        return Arrays.asList(new Object[][] {
                {"/app1", "unreachable"}, //unreachable - no explicitly mentioned resource
                {"/app1ann", "helloworld1"}, //app1ann - overridden using a servlet-mapping element in the web.xml
                {"/app2ann", "unreachable"}, //unreachable - no explicitly mentioned resource
                {"/app3ann", "unreachable"}, //unreachable - no explicitly mentioned resource
                {"/app4", "unreachable"}, //unreachable - no explicitly mentioned resource
        });
    }

    @Override
    protected Application configure() {
        return new Application();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testUnreachableResource() {
        WebTarget t = target(appPath);
        t.register(LoggingFeature.class);
        Response r = t.path(resourcePath).request().get();
        assertEquals(404, r.getStatus());
    }

}
