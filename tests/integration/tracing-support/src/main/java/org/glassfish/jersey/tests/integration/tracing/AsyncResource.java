/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.tracing;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

/**
* @author Libor Kramolis
*/
@Path("/async")
public class AsyncResource {

    @Path("{name}")
    @GET
    public void get(@PathParam("name") String name, @Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(new Message(new StringBuffer(name).reverse().toString()));
    }

    @POST
    public void post(Message post, @Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(new Message(new StringBuffer(post.getText()).reverse().toString()));
    }

    @Path("sub-resource-method")
    @POST
    public void postSub(Message post, @Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(new Message(new StringBuffer(post.getText()).reverse().toString()));
    }

    @Path("sub-resource-locator")
    public AsyncSubResource getSubLoc() {
        return new AsyncSubResource();
    }

    @Path("sub-resource-locator-null")
    public AsyncSubResource getSubLocNull() {
        return null;
    }

    @GET
    @Path("runtime-exception")
    public void getRuntimeException(@Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(new RuntimeException("Something does not work ..."));
    }

    @GET
    @Path("mapped-exception")
    public void getMappedException(@Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(new TestException("This could be client fault ..."));
    }

}
