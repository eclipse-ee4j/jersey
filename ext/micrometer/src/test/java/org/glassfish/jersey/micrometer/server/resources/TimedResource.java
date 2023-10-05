/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.micrometer.server.resources;

import java.util.concurrent.CountDownLatch;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.micrometer.core.annotation.Timed;

import static java.util.Objects.requireNonNull;

/**
 * @author Michael Weirauch
 */
@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class TimedResource {

    private final CountDownLatch longTaskRequestStartedLatch;

    private final CountDownLatch longTaskRequestReleaseLatch;

    public TimedResource(CountDownLatch longTaskRequestStartedLatch, CountDownLatch longTaskRequestReleaseLatch) {
        this.longTaskRequestStartedLatch = requireNonNull(longTaskRequestStartedLatch);
        this.longTaskRequestReleaseLatch = requireNonNull(longTaskRequestReleaseLatch);
    }

    @GET
    @Path("not-timed")
    public String notTimed() {
        return "not-timed";
    }

    @GET
    @Path("timed")
    @Timed
    public String timed() {
        return "timed";
    }

    @GET
    @Path("multi-timed")
    @Timed("multi1")
    @Timed("multi2")
    public String multiTimed() {
        return "multi-timed";
    }

    /*
     * Async server side processing (AsyncResponse) is not supported in the in-memory test
     * container.
     */
    @GET
    @Path("long-timed")
    @Timed
    @Timed(value = "long.task.in.request", longTask = true)
    public String longTimed() {
        longTaskRequestStartedLatch.countDown();
        try {
            longTaskRequestReleaseLatch.await();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "long-timed";
    }

    @GET
    @Path("just-long-timed")
    @Timed(value = "long.task.in.request", longTask = true)
    public String justLongTimed() {
        longTaskRequestStartedLatch.countDown();
        try {
            longTaskRequestReleaseLatch.await();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "long-timed";
    }

    @GET
    @Path("long-timed-unnamed")
    @Timed
    @Timed(longTask = true)
    public String longTimedUnnamed() {
        return "long-timed-unnamed";
    }

}
