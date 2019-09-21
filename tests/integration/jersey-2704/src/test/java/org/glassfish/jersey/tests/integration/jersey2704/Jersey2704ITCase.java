/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2704;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.glassfish.jersey.tests.integration.jersey2704.services.HappyService;
import org.glassfish.jersey.tests.integration.jersey2704.services.SadService;
import org.junit.Assert;
import org.junit.Test;


/**
 * This test case is to cover enhancement implemented in JERSEY-2704. The goal of this enhancement
 * is to give users possibility to register main {@link ServiceLocator} in the servlet context, so
 * it can be later used by Jersey. This creates the opportunity to wire Jersey-specific classes with
 * the services created outside the Jersey context.
 *
 * @author Bartosz Firyn (bartoszfiryn at gmail.com)
 */
public class Jersey2704ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new TestApplication();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    /**
     * Invokes REST endpoint to check whether specific class service is registered in the
     * {@link ServiceLocator}.
     *
     * @param service the service class
     * @return HTTP status code, 200 when service is available and 600 otherwise
     * @throws IOException in case of problems with HTTP communication
     */
    private int test(Class<?> service) throws IOException {

        String name = service.getCanonicalName();
        String path = getBaseUri().toString() + "test/" + name;

        HttpURLConnection connection = (HttpURLConnection) new URL(path).openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        connection.disconnect();

        return connection.getResponseCode();
    }

    /**
     * Test to cover sunny day scenario, i.e. specific service has been registered in the parent
     * {@link ServiceLocator} so it will be available in the one that is used in Jersey context.
     *
     * @throws IOException
     */
    @Test
    public void testCorrectInjection() throws IOException {
        Assert.assertEquals(200, test(HappyService.class));
    }

    /**
     * Test to cover rainy day scenario, i.e. specific service has <b>not</b> been registered in the
     * parent {@link ServiceLocator} so it cannot be used to wire Jersey classes.
     *
     * @throws IOException
     */
    @Test
    public void testMisingInjection() throws IOException {
        Assert.assertEquals(600, test(SadService.class));
    }
}
