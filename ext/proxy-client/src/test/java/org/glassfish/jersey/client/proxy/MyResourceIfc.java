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

package org.glassfish.jersey.client.proxy;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path("myresource")
public interface MyResourceIfc {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getIt();

    @POST
    @Consumes({MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML})
    List<MyBean> postIt(List<MyBean> entity);

    @POST
    @Path("valid")
    @Consumes({MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_XML})
    MyBean postValid(@Valid MyBean entity);

    @Path("{id}")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getId(@PathParam("id") String id);

    @Path("query")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByName(@QueryParam("name") String name);

    @Path("cookie")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameCookie(@CookieParam("cookie-name") String name);

    @Path("header")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameHeader(@HeaderParam("header-name") String name);

    @Path("matrix")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameMatrix(@MatrixParam("matrix-name") String name);

    @Path("form")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    String postByNameFormParam(@FormParam("form-name") String name);


    @Path("query-list")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameList(@QueryParam("name-list") List<String> name);

    @Path("query-set")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameSet(@QueryParam("name-set") Set<String> name);

    @Path("query-sortedset")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameSortedSet(@QueryParam("name-sorted") SortedSet<String> name);

    @Path("cookie-list")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameCookieList(@CookieParam("cookie-name-list") List<String> name);

    @Path("cookie-set")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameCookieSet(@CookieParam("cookie-name-set") Set<String> name);

    @Path("cookie-sortedset")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameCookieSortedSet(@CookieParam("cookie-name-sorted") SortedSet<String> name);

    @Path("header-list")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameHeaderList(@HeaderParam("header-name-list") List<String> name);

    @Path("header-set")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameHeaderSet(@HeaderParam("header-name-set") Set<String> name);

    @Path("header-sortedset")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameHeaderSortedSet(@HeaderParam("header-name-sorted") SortedSet<String> name);

    @Path("matrix-list")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameMatrixList(@MatrixParam("matrix-name-list") List<String> name);

    @Path("matrix-set")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameMatrixSet(@MatrixParam("matrix-name-set") Set<String> name);

    @Path("matrix-sortedset")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    String getByNameMatrixSortedSet(@MatrixParam("matrix-name-sorted") SortedSet<String> name);

    @Path("form-list")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    String postByNameFormList(@FormParam("form-name-list") List<String> name);

    @Path("form-set")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    String postByNameFormSet(@FormParam("form-name-set") Set<String> name);

    @Path("form-sortedset")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    String postByNameFormSortedSet(@FormParam("form-name-sorted") SortedSet<String> name);

    @Path("subresource")
    MySubResourceIfc getSubResource();

    @Path("isAcceptHeaderValid")
    @GET
    @Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_XML})
    boolean isAcceptHeaderValid(@Context HttpHeaders headers);

    @Path("putIt")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    String putIt(MyBean dummyBean);
}
