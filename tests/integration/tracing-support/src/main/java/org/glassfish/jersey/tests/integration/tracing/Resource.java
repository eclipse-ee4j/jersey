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

package org.glassfish.jersey.tests.integration.tracing;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
* @author Libor Kramolis (libor.kramolis at oracle.com)
*/
@Path("/root")
public class Resource {

    @Path("{name}")
    @GET
    public Message get(@PathParam("name") String name) {
        return new Message(new StringBuffer(name).reverse().toString());
    }

    @POST
    public Message post(Message post) {
        return new Message(new StringBuffer(post.getText()).reverse().toString());
    }

    @Path("sub-resource-method")
    @POST
    public Message postSub(Message post) {
        return new Message(new StringBuffer(post.getText()).reverse().toString());
    }

    @Path("sub-resource-locator")
    public SubResource getSubLoc() {
        return new SubResource();
    }

    @Path("sub-resource-locator-null")
    public SubResource getSubLocNull() {
        return null;
    }

    @GET
    @Path("runtime-exception")
    public Message getRuntimeException() {
        throw new RuntimeException("Something does not work ...");
    }

    @GET
    @Path("mapped-exception")
    public Message getMappedException() {
        throw new TestException("This could be client fault ...");
    }

}
