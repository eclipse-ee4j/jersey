/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_25_init_1;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Reproducer for JERSEY-1801. See also E2E {@code LinkTest}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("links")
public class MultipleLinksResource {

    @GET
    public Response get(@Context UriInfo uriInfo) throws Exception {
        URI test1 = URI.create(uriInfo.getAbsolutePath().toString() + "test1");
        URI test2 = URI.create(uriInfo.getAbsolutePath().toString() + "test2");

        return Response.ok()
                .link("http://oracle.com", "parent")
                .link(new URI("http://jersey.java.net"), "framework")
                .links(
                        Link.fromUri(uriInfo.relativize(test1)).rel("test1").build(),
                        Link.fromUri(test2).rel("test2").build(),
                        Link.fromUri(uriInfo.relativize(URI.create("links/test3"))).rel("test3").build()
                ).build();
    }
}
