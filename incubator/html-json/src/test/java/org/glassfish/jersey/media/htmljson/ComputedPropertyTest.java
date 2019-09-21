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

package org.glassfish.jersey.media.htmljson;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Model;
import net.java.html.json.Property;

/** Can use the same code on server as well as client.
 *
 * @author Jaroslav Tulach
 */
@Model(className = "Person", properties = {
        @Property(name = "firstName", type = String.class),
        @Property(name = "lastName", type = String.class)
})
public class ComputedPropertyTest extends AbstractTypeTester {

    @ComputedProperty
    static String fullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }

    //
    // server side
    //
    @Path("empty")
    public static class TestResource {

        @PUT
        @Path("fullName")
        public String myBean(Person p) {
            return p.getFullName();
        }
    }

    //
    // Client using the model to connect to server
    //

    @Test
    public void askForFullName() {
        WebTarget target = target("empty/fullName");

        Person p = new Person();
        p.setFirstName("Jaroslav");
        p.setLastName("Tulach");

        final Response response = target.request().put(Entity.entity(p, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());
        assertEquals("Jaroslav Tulach", response.readEntity(String.class));
    }
}
