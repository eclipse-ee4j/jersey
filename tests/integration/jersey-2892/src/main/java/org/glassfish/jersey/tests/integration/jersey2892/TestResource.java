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

package org.glassfish.jersey.tests.integration.jersey2892;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.xml.bind.annotation.XmlTransient;

/**
 * A resource that provides a means to test whether repeating classes in object graph are correctly filtered out.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    @GET
    @Path("pointer")
    public Pointer pointer() {
        return new Pointer();
    }

    public static class Pointer {

        public final Persons persons = new Persons();
    }

    @GET
    @Path("test")
    public Persons issue() {
        return new Persons();
    }

    public static class Persons {

        public final Person first = new Person("Larry", "Amphitheatre Pkwy", 1600, "Mountain View");
        public final Person second = new Person("Bill", "Microsoft Way", 1, "Redmond");
    }

    public static class Person {

        public Person() {
        }

        public Person(final String name, final String streetName, final int streetNumber, final String city) {
            this.name = name;
            address = new Address(streetName, streetNumber, city);
        }

        public Address address;
        public String name;
    }

    public static class Address {

        public Address() {
        }

        public Address(final String name, final int number, final String city) {
            this.city = city;
            street = new Street(name, number);
        }

        public Street street;
        public String city;
    }

    public static class Street {

        public Street() {
        }

        public Street(final String name, final int number) {
            this.name = name;
            this.number = number;
        }

        public String name;
        public int number;
    }

    @GET
    @Path("recursive")
    public Recursive recursive() {
        return new Recursive();
    }

    public static class Recursive {

        public String idRecursive = "a";
        public SubField subField = new SubField();
    }

    public static class SubField {

        public final String idSubField = "b";
        public final SubSubField subSubField;

        public SubField() {
            subSubField = new SubSubField(this);
        }
    }

    public static class SubSubField {

        public final String idSubSubField = "c";

        @XmlTransient
        public SubField subField;

        public SubSubField() {
        }

        public SubSubField(final SubField subField) {
            this.subField = subField;
        }
    }
}
