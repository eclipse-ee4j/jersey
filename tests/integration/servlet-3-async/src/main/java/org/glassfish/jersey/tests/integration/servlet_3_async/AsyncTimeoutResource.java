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

package org.glassfish.jersey.tests.integration.servlet_3_async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;

/**
 * Asynchronous servlet-deployed resource for testing {@link javax.ws.rs.container.AsyncResponse async response} timeouts.
 *
 * @author Michal Gajdos
 */
@Path("timeout")
public class AsyncTimeoutResource {

    private static final BlockingQueue<AsyncResponse> queue = new ArrayBlockingQueue<AsyncResponse>(1);

    @GET
    @Path("suspend")
    public void suspend(@Suspended final AsyncResponse asyncResponse) {
        queue.add(asyncResponse);
    }

    @POST
    @Path("timeout")
    public void setTimeOut(final Integer millis) throws InterruptedException {
        final AsyncResponse asyncResponse = queue.take();

        final boolean timeout1 = asyncResponse.setTimeout(millis, TimeUnit.MILLISECONDS);
        asyncResponse.setTimeoutHandler(new TimeoutHandler() {

            @Override
            public void handleTimeout(final AsyncResponse asyncResponse) {
                final boolean timeout2 = asyncResponse.setTimeout(millis, TimeUnit.MILLISECONDS);
                asyncResponse.setTimeoutHandler(new TimeoutHandler() {

                    @Override
                    public void handleTimeout(final AsyncResponse asyncResponse) {
                        asyncResponse.resume("timeout1=" + timeout1 + "_timeout2=" + timeout2 + "_handled");
                        asyncResponse.cancel();
                    }
                });
            }
        });
    }
}
