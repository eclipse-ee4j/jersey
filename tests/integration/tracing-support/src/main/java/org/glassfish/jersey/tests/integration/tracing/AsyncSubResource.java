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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

/**
* @author Libor Kramolis
*/
@Path("/")
public class AsyncSubResource {
    @POST
    public void post(Message post, @Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(new Message(new StringBuffer(post.getText()).reverse().toString()));
    }

    @Path("sub-resource-method")
    @POST
    public void postSub(Message post, @Suspended final AsyncResponse asyncResponse) {
        asyncResponse.resume(new Message(new StringBuffer(post.getText()).reverse().toString()));
    }
}
