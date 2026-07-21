/*
 * Copyright (c) 2020, 2022, 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson3.internal.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.Optional;

@Path("/entity/")
public final class ServiceTest {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/simple")
    public EntityTest simple() {
        return new EntityTest("Hello", "World");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/exchange")
    public EntityTest exchange(EntityTest entity) {
        return new EntityTest(entity.getName(), "Universe");
    }

    public static final class EntityTest {

        private String name;
        private String value;

        public EntityTest() {
            // For deserialization without NoCtorDeserModule
        }

        public EntityTest(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        @XmlElement(name = "jaxb")
        @JsonGetter("name")
        public final String getName() {
            return name;
        }

        @JsonGetter("value")
        public final Optional<String> getValue() {
            return Optional.ofNullable(value);
        }
    }




}
