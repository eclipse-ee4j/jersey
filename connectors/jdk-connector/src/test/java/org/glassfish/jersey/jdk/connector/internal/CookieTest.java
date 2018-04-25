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

package org.glassfish.jersey.jdk.connector.internal;

import java.net.CookiePolicy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jdk.connector.JdkConnectorProperties;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Paul Sandoz (paul.sandoz at oracle.com)
 * @author Arul Dhesiaseelan (aruld at acm.org)
 */
public class CookieTest extends JerseyTest {

    @Path("/CookieResource")
    public static class CookieResource {

        @GET
        public Response get(@Context HttpHeaders h) {
            Cookie c = h.getCookies().get("name");
            String e = (c == null) ? "NO-COOKIE" : c.getValue();
            return Response.ok(e).cookie(new NewCookie("name", "value")).build();
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(CookieResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(new JdkConnectorProvider());
    }

    @Test
    public void testCookieResource() {
        // the default cookie policy does not like cookies from localhost
        WebTarget target = target("CookieResource").property(JdkConnectorProperties.COOKIE_POLICY, CookiePolicy.ACCEPT_ALL);

        assertEquals("NO-COOKIE", target.request().get(String.class));
        assertEquals("value", target.request().get(String.class));
    }

    @Test
    public void testDisabledCookies() {
        // the default cookie policy does not like cookies from localhost
        WebTarget target = target("CookieResource").property(JdkConnectorProperties.COOKIE_POLICY, CookiePolicy.ACCEPT_NONE);

        assertEquals("NO-COOKIE", target.request().get(String.class));
        assertEquals("NO-COOKIE", target.request().get(String.class));
    }
}
