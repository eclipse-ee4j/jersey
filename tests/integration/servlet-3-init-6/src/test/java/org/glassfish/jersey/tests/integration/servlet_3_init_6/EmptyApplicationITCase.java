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

package org.glassfish.jersey.tests.integration.servlet_3_init_6;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.tests.integration.servlet_3_init_6.resource.ResourceOne;
import org.glassfish.jersey.tests.integration.servlet_3_init_6.resource.ResourceTwo;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class EmptyApplicationITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new EmptyApplication();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testResourceOne() throws Exception {
        final Response response = target("one").request().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(ResourceOne.class.getSimpleName()));
    }

    @Test
    public void testResourceTwo() throws Exception {
        final Response response = target("two").request().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is(ResourceTwo.class.getSimpleName()));
    }
}
