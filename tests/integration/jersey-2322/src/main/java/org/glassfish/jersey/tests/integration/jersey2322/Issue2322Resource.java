/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2322;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Test resource.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@Path("/")
@Consumes("application/json")
@Produces("application/json")
public class Issue2322Resource {

    public static class JsonString1 {
        private String value;

        public JsonString1() {
        }
        public JsonString1(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class JsonString2 {
        private String value;

        public JsonString2() {
        }
        public JsonString2(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }

    @Path("1")
    @PUT
    public JsonString1 put(final JsonString1 wrapper) {
        return new JsonString1("Hello " + wrapper.getValue());
    }

    @Path("2")
    @PUT
    public JsonString2 put(final JsonString2 wrapper) {
        return new JsonString2("Hi " + wrapper.getValue());
    }

}
