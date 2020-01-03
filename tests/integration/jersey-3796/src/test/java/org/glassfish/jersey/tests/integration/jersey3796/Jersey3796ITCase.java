/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey3796;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Tests the one class initialization per all declared provider interfaces
 */
public class Jersey3796ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        return new MyApplication();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testSameInstanceForProviders() {
        final Map response = target("/myresource").request(MediaType.APPLICATION_JSON_TYPE).get(Map.class);
        //Map shall not be null
        Assert.assertNotNull(response);
        //Map shall contain exactly three elements
        Assert.assertEquals(3, response.size());
        //Map shall contain ony keys Feature, Request and Response
        //Values of that keys shall be equals.
        //Equality of all values indicates the class is only one per all tested providers
        Assert.assertEquals(response.get("Feature"), response.get("Request"));
        Assert.assertEquals(response.get("Feature"), response.get("Response"));
        Assert.assertEquals(response.get("Response"), response.get("Request"));
    }
}
