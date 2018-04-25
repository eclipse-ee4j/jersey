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

package org.glassfish.jersey.tests.memleaks.testleak;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import javax.inject.Singleton;

/**
 * Resource that causes {@link OutOfMemoryError} exception upon repetitive call of {@link #invoke(int)}.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@Path("/")
@Singleton
public class MemoryLeakingResource {

    final List<Object> leakingList = new LinkedList<>();

    @POST
    @Path("invoke")
    public String invoke(@DefaultValue("1048576") @QueryParam("size") int size) {
        byte[] bytes = new byte[size];
        leakingList.add(bytes);

        return String.valueOf(leakingList.size());
    }

    @GET
    @Path("hello")
    @Produces("text/plain")
    public String helloWorld() {
        System.out.println("HELLO WORLD!");
        return "HELLO WORLD!";
    }

}
