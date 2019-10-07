/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.mbw.custom;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;

/**
 * Person resource.
 *
 * @author Jakub Podlesak
 */
@Path("/")
@Consumes("application/person")
@Produces("application/person")
public class PersonResource {

    @POST
    public Person echo(final Person person) {
        return person;
    }

    @PUT
    public void put(final Person text) {
    }

    @GET
    public Person get() {
        return new Person("Mozart", 21, "Salzburg");
    }
}
