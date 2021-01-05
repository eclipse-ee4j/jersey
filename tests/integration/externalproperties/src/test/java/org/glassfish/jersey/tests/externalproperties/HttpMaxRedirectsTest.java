/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.externalproperties;

import org.glassfish.jersey.ExternalProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.net.URI;

public class HttpMaxRedirectsTest extends JerseyTest {

    @Path("resource")
    public static class RedirectResource {

        @GET
        public Response getRedirect1() {
            return Response
                    .seeOther(URI.create("resource/redirect1"))
                    .build();
        }

        @GET
        @Path("/redirect1")
        public Response getRedirect2() {
            return Response
                    .seeOther(URI.create("resource/redirect2"))
                    .build();
        }

        @GET
        @Path("/redirect2")
        public Response getRedirect3() {
            return Response
                    .seeOther(URI.create("resource/finalredirect"))
                    .build();
        }

        @GET
        @Path("/finalredirect")
        public String finalDestination() {
            return "You have been redirected 3 times !";
        }

    }

    @Override
    protected Application configure() {
        return new ResourceConfig(RedirectResource.class);
    }

    @Test
    public void testFailRedirect() {
        System.setProperty(ExternalProperties.HTTP_MAX_REDIRECTS, "1");

        try {
            target("resource").request().get();
            Assert.fail("Should have thrown ProcessingException"
                    + " because of too many redirects.");
        } catch (ProcessingException pe) {
            Assert.assertEquals("java.net.ProtocolException: Server redirected too many  times (1)",
                    pe.getMessage());
        }

    }

}
