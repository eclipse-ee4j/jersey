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

package org.glassfish.jersey.tests.integration.servlet_3_init_provider;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import org.junit.Assert;

import javax.ws.rs.NotFoundException;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public abstract class AbstractHelloWorldResourceTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);

        return new ResourceConfig(getResourceClass());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testHelloWorld() throws Exception {
        for (int i = 1; i <= AbstractHelloWorldResource.NUMBER_OF_APPLICATIONS; i++) {
            try {
                String actual = target("application" + getIndex()).path("helloworld" + i).request().get(String.class);
                if (i == getIndex()) {
                    Assert.assertEquals("Hello World #" + getIndex() + "!", actual);
                } else {
                    Assert.fail("i: " + i + " | [" + actual + "]");
                }
            } catch (NotFoundException ex) {
                if (i != getIndex()) {
                    Assert.assertEquals(404, ex.getResponse().getStatus());
                } else {
                    Assert.fail("!!! i: " + i);
                }
            }
        }
    }

    protected abstract Class<?> getResourceClass();

    protected abstract int getIndex();

}
