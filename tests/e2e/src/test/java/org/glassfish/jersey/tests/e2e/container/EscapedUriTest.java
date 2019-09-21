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

package org.glassfish.jersey.tests.e2e.container;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class EscapedUriTest extends JerseyContainerTest {

    private static final String RESPONSE = "CONTENT";

    @Path("x%20y")
    public static class EscapedUriResource {

        private final String context;

        @SuppressWarnings("UnusedDeclaration")
        public EscapedUriResource() {
            this("");
        }

        public EscapedUriResource(final String context) {
            this.context = context;
        }

        @GET
        public String get(@Context final UriInfo info) {
            assertEquals(context + "/x%20y", info.getAbsolutePath().getRawPath());
            assertEquals("/", info.getBaseUri().getRawPath());
            assertEquals(context + "/x y", "/" + info.getPath());
            assertEquals(context + "/x%20y", "/" + info.getPath(false));

            return RESPONSE;
        }
    }

    @Path("non/x y")
    public static class NonEscapedUriResource extends EscapedUriResource {

        public NonEscapedUriResource() {
            super("/non");
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(EscapedUriResource.class, NonEscapedUriResource.class);
    }

    @Test
    public void testEscaped() {
        assertThat(target("x%20y").request().get(String.class), is(RESPONSE));
    }

    @Test
    public void testNonEscaped() {
        assertThat(target("non/x y").request().get(String.class), is(RESPONSE));
    }
}
