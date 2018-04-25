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

package org.glassfish.jersey.tests.integration.jersey1960;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Reproducer tests for JERSEY-1960.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class Jersey1960ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new Jersey1960App();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    /**
     * Reproducer method for JERSEY-1960.
     */
    @Test
    public void testJersey1960Fix() {
        for (int i = 0; i < 10; i++) {
            String response = target().path("jersey-1960/echo").request().header(RequestFilter.REQUEST_NUMBER, i)
                    .post(Entity.text("test"), String.class);
            // Assert that the request has been filtered and processed by the echo method.
            assertEquals(new EchoResource().echo("filtered-1111-" + i), response);
        }
    }
}
