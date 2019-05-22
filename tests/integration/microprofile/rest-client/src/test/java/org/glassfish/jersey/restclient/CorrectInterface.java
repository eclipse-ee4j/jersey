/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.restclient;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

/**
 * Correct test interface for validation
 *
 * @author David Kral
 */

@Path("test/{first}")
public interface CorrectInterface {

    @GET
    @Path("{second}")
    @ClientHeaderParam(name = "test", value = "someValue")
    void firstMethod(@PathParam("first") String first, @PathParam("second") String second);

    @GET
    @ClientHeaderParam(name = "test", value = "{value}")
    void secondMethod(@PathParam("first") String first, String second);

    @POST
    @ClientHeaderParam(name = "test",
                       value = "org.glassfish.jersey.org.glassfish.jersey.restclient.CustomHeaderGenerator.customHeader")
    void thirdMethod(@PathParam("first") String first);

    @GET
    @Path("{second}")
    void fourthMethod(@PathParam("first") String first, @BeanParam BeanWithPathParam second);

    default String value() {
        return "testValue";
    }

    class CustomHeaderGenerator {

        public static String customHeader() {
            return "static";
        }

    }

    class BeanWithPathParam {

        @PathParam("second")
        public String pathParam;

    }

}
