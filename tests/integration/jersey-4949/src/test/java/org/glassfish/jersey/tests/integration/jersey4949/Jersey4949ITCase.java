/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey4949;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Reproducer tests for JERSEY-4949.
 */
public class Jersey4949ITCase extends JerseyTest {

    private static final String CONTEXT_PATH = "c%20ntext";
    private static final String SERVLET_PATH = "A%20B";

    @Override
    protected Application configure() {
        return new Jersey4949();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        //return new org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory();
        return new ExternalTestContainerFactory();
    }

    /**
     * Reproducer method for JERSEY-4949.
     */
    @Test
    public void testJersey4949Fix() {
        try (Response response = target(CONTEXT_PATH).path(SERVLET_PATH).path(Issue4949Resource.PATH).request().get()) {
            assertThat(response.getStatus(), is(200));

            String entity = response.readEntity(String.class);
            Assert.assertTrue(entity.contains(CONTEXT_PATH));
            Assert.assertTrue(entity.contains(SERVLET_PATH));
            Assert.assertTrue(entity.contains(Issue4949Resource.PATH));
        }
    }
}
