/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;

/**
 * Person resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
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
