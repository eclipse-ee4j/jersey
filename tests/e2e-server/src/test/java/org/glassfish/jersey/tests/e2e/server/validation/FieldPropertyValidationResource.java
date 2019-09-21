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

package org.glassfish.jersey.tests.e2e.server.validation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Michal Gajdos
 */
@Path("fieldPropertyValidationResource")
public class FieldPropertyValidationResource {

    @Path("valid")
    public SubResource valid() {
        return new SubResource("valid", "valid", "valid", "valid", "valid", "valid");
    }

    @Path("invalidPropertyGetterAndClassNull")
    public SubResource invalidPropertyGetterAndClassNull() {
        return new SubResource("valid", "valid", "valid", "valid", "valid", null);
    }

    @Path("invalidPropertyGetterAndClassLong")
    public SubResource invalidPropertyGetterAndClassLong() {
        return new SubResource("valid", "valid", "valid", "valid", "valid", "valid-valid");
    }

    @Path("invalidPropertyAndClassNull")
    public SubResource invalidPropertyAndClassNull() {
        return new SubResource("valid", "valid", "valid", "valid", null, "valid");
    }

    @Path("invalidFieldAndClassNull")
    public SubResource invalidFieldAndClassNull() {
        return new SubResource("valid", "valid", "valid", null, "valid", "valid");
    }

    @Path("invalidPropertyGetterNull")
    public SubResource invalidPropertyGetterNull() {
        return new SubResource("valid", "valid", null, "valid", "valid", "valid");
    }

    @Path("invalidPropertyGetterLong")
    public SubResource invalidPropertyGetterLong() {
        return new SubResource("valid", "valid", "valid-valid", "valid", "valid", "valid");
    }

    @Path("invalidPropertyNull")
    public SubResource invalidPropertyNull() {
        return new SubResource("valid", null, "valid", "valid", "valid", "valid");
    }

    @Path("invalidFieldNull")
    public SubResource invalidFieldNull() {
        return new SubResource(null, "valid", "valid", null, "valid", "valid");
    }

    @FieldPropertyValidation(elements = {"fieldAndClass", "propertyAndClass", "propertyGetterAndClass"})
    public static class SubResource {

        @NotNull
        @Size(min = 5)
        final String field;

        @NotNull
        @Size(min = 5)
        final String property;

        @NotNull
        @Size(min = 5)
        final String propertyGetter;

        final String fieldAndClass;

        final String propertyAndClass;

        final String propertyGetterAndClass;

        public SubResource(final String field, final String property, final String propertyAndGetter,
                           final String fieldAndClass, final String propertyAndClass, final String propertyGetterAndClass) {
            this.field = field;
            this.property = property;
            this.propertyGetter = propertyAndGetter;
            this.fieldAndClass = fieldAndClass;
            this.propertyAndClass = propertyAndClass;
            this.propertyGetterAndClass = propertyGetterAndClass;
        }

        public String getProperty() {
            return property;
        }

        @Size(max = 5)
        public String getPropertyGetter() {
            return propertyGetter;
        }

        public String getPropertyAndClass() {
            return propertyAndClass;
        }

        @Size(max = 5)
        public String getPropertyGetterAndClass() {
            return propertyGetterAndClass;
        }

        @GET
        public String method() {
            return "ok";
        }
    }
}
