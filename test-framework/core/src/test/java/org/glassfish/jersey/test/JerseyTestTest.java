/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test;

import java.net.URI;
import java.security.AccessController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * {@link org.glassfish.jersey.test.JerseyTest} unit tests.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class JerseyTestTest {

    @Path("/")
    public static class MyResource {

        @GET
        public String get() {
            return "xxx";
        }
    }

    public static class MyTestContainerFactory implements TestContainerFactory {

        @Override
        public TestContainer create(final URI baseUri, final DeploymentContext context) throws IllegalArgumentException {
            return new TestContainer() {

                @Override
                public ClientConfig getClientConfig() {
                    return null;
                }

                @Override
                public URI getBaseUri() {
                    return baseUri;
                }

                @Override
                public void start() {
                }

                @Override
                public void stop() {
                }
            };
        }
    }

    private static class MyJerseyTest extends JerseyTest {
        @Override
        protected Application configure() {
            return new ResourceConfig(MyResource.class);
        }

        @Override
        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
            return new MyTestContainerFactory();
        }
    }

    @Test
    public void testCustomTestContainerFactory() {
        MyJerseyTest myJerseyTest = new MyJerseyTest();

        assertEquals(myJerseyTest.getTestContainerFactory().getClass(), MyTestContainerFactory.class);
    }

    @Test
    public void testOverridePortNumber() {
        final int newPort = TestProperties.DEFAULT_CONTAINER_PORT + 1;
        MyJerseyTest myJerseyTest = new MyJerseyTest() {
            @Override
            protected Application configure() {
                forceSet(TestProperties.CONTAINER_PORT, Integer.toString(newPort));
                return super.configure();
            }
        };

        assertEquals(newPort, myJerseyTest.getPort());
    }

    @Test
    public void testThatDefaultContainerPortIsUsed() {
        MyJerseyTest myJerseyTest = new MyJerseyTest();

        String portValue = AccessController.doPrivileged(PropertiesHelper.getSystemProperty(TestProperties.CONTAINER_PORT,
                String.valueOf(TestProperties.DEFAULT_CONTAINER_PORT)));

        assertEquals(Integer.valueOf(portValue).intValue(), myJerseyTest.getPort());
    }

}
