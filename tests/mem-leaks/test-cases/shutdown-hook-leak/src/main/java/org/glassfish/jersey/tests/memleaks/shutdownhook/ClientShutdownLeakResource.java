/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.memleaks.shutdownhook;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import javax.inject.Singleton;

/**
 * This resource reproduces JERSEY-2786 when {@link #invokeClient()} called repetitively.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@Path("client")
@Singleton
public class ClientShutdownLeakResource {

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target("http://example.com");

    @POST
    @Path("invoke")
    public String invokeClient() {

        WebTarget target2 = target.property("Washington", "Irving");
        Invocation.Builder req = target2.request().property("how", "now");
        req.buildGet().property("Irving", "Washington");

        return target.toString();

    }

    @GET
    @Path("helloworld")
    @Produces("text/plain")
    public String helloWorld() {
        return "HELLO WORLD!";
    }

}
