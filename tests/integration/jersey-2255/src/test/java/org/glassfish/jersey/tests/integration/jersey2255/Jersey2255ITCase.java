/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2255;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.tests.integration.jersey2255.Issue2255Resource.A;
import org.glassfish.jersey.tests.integration.jersey2255.Issue2255Resource.B;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

/**
 * Reproducer tests for JERSEY-2255.
 *
 * @author Eric Miles (emilesvt at gmail.com)
 */
public class Jersey2255ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new Jersey2255();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    /**
     * Server side response is wrapped, needs to be read to wrapper class.
     */
    @Test
    public void testClassAGet() {
        final Response response = target("A").request().get();
        final A entity = response.readEntity(A.class);

        assertThat(response.getStatus(), equalTo(200));
        assertNull(entity.getFieldA1());
    }

    @Test
    public void testDetailedClassAGet() {
        final Response response = target("A").queryParam("detailed", true).request().get();
        final A entity = response.readEntity(A.class);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(entity.getFieldA1(), equalTo("fieldA1Value"));
    }

    /**
     * Server side response is returned as orig class.
     */
    @Test
    public void testDetailedClassBGet() {
        final Response response = target("B").queryParam("detailed", true).request().get();
        final B entity = response.readEntity(B.class);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(entity.getFieldA1(), equalTo("fieldA1Value"));
        assertThat(entity.getFieldB1(), equalTo("fieldB1Value"));
    }

    @Test
    public void testClassBGet() {
        final Response response = target("B").request().get();
        final B entity = response.readEntity(B.class);

        assertThat(response.getStatus(), equalTo(200));
        assertNull(entity.getFieldA1());
        assertThat(entity.getFieldB1(), equalTo("fieldB1Value"));
    }
}
