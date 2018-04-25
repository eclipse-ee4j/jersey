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

package org.glassfish.jersey.tests.integration.j376;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class J376Test {
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(GrizzlyApp.getBaseUri());

    @BeforeClass
    public static void setUpTest() {
        GrizzlyApp.start();
    }

    @AfterClass
    public static void tearDownTest() {
        GrizzlyApp.stop();
    }

    @Test
    public void testConstructorInjection() {
        final String response = target.path("constructor").request().post(Entity.entity("name=John&age=32",
                MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

        assertEquals("John:32:Hello:constructor", response);
    }

    @Test
    public void testFieldInjection() {
        final String response = target.path("field").request().post(Entity.entity("name=Bill&age=21",
                MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

        assertEquals("Bill:21:Hello:field", response);
    }

    @Test
    public void testMethodInjection() {
        final String response = target.path("method").request().post(Entity.entity("name=Mike&age=42",
                MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

        assertEquals("Mike:42:Hello:method", response);
    }

    @Test
    public void testAppScopedBeanInReqScopedResource() {
        final String response = target.path("field/appScoped").request().get(String.class);
        assertEquals("ApplicationScopedBean:Hello", response);
    }

    @Test
    public void testAppScopedResource() {
        String response = target.path("appScope/msg").request().get(String.class);
        assertEquals("ApplicationScopedBean:Hello", response);
        response = target.path("appScope/uri").request().get(String.class);
        assertEquals("appScope/uri", response);
        response = target.path("appScope/req").request().get(String.class);
        assertEquals("Hello", response);
    }

    @Test
    public void testBeanParamInAppScoped() {
        final String response = target.path("appScope").request().post(Entity.entity("name=John&age=35",
                MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

        assertEquals("John:35:Hello:appScope", response);
    }

    @Test
    public void testContextInjectionInAppScopedBean() {
        final String response = target.path("field/appScopedUri").request().get(String.class);
        assertEquals("field/appScopedUri", response);

    }
}
