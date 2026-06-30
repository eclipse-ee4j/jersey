/*
 * Copyright (c) 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.jettyresponseclose;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Response;

import java.lang.System.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.System.Logger.Level.INFO;

@Path("/get-me-204")
public class Resource204 {
    private static final Logger LOG = System.getLogger(Resource204.class.getName());
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(200);

    @GET
    public void get(@Suspended final AsyncResponse ar) {
        EXECUTOR.schedule(() -> {
            LOG.log(INFO, () -> "Resuming " + ar);
            ar.resume(Response.noContent().build());
            LOG.log(INFO, () -> "Processing finished: " + ar);
        }, 100, TimeUnit.MILLISECONDS);
    }
}
