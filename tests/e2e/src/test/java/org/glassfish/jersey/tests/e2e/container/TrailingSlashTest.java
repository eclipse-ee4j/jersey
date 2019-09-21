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
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Michal Gajdos
 */
public class TrailingSlashTest extends JerseyContainerTest {

    @Path("/")
    public static class Resource {

        @GET
        @Path("get")
        public String get() {
            return "get";
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(Resource.class);
    }

    @Test
    public void testSlash() throws Exception {
        _test("get/");
    }

    @Test
    public void testNoSlash() throws Exception {
        _test("get");
    }

    private void _test(final String path) {
        final Response response = target(path).request().get();

        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(String.class), is("get"));
    }
}
