/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;

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
 * @author Paul Sandoz
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

        @Path("/issue4678")
        @GET
        public Response issue4678(@Context HttpHeaders h) {
            // Read the cookie
            Cookie c = h.getCookies().get("foo");
            // Write the value in a new cookie foo2. So we test cookies in both ways.
            return Response.ok().header(HttpHeaders.SET_COOKIE,
                    "foo2=" + c.getValue() + "; expires=Wed, 10-Feb-2021 16:16:26 GMT; HttpOnly; Path=/; SameSite=Lax")
                    .build();
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

    @Test
    public void testIssue4678() {
        Response response = target("/CookieResource/issue4678")
                .request().header(HttpHeaders.COOKIE,
                        "foo=bar; expires=Wed, 10-Feb-2021 16:16:26 GMT; HttpOnly; Path=/; SameSite=Lax")
                .get();
        // Issue 4678 happens here. HttpParser splits the headers value by comma.
        List<Object> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertEquals("Expected 1 cookie, but it received: " + setCookies, 1, setCookies.size());
        NewCookie newCookie = response.getCookies().get("foo2");
        assertEquals("bar", newCookie.getValue());
    }

}
