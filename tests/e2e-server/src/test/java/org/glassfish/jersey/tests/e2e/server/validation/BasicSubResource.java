/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Michal Gajdos
 */
@NonEmptyNames
@ValidateOnExecution(type = ExecutableType.ALL)
public class BasicSubResource {

    @NotNull
    @FormParam("firstName")
    private String firstName;

    @NotNull
    @FormParam("lastName")
    private String lastName;

    private String email;

    /**
     * Note: Constructor input parameter should not be validated.
     */
    @SuppressWarnings("UnusedParameters")
    public BasicSubResource(@NotNull @Context final ResourceContext resourceContext) {
    }

    @FormParam("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @NotNull
    @Email(regexp = "[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")
    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/contactBean")
    @Valid
    public ContactBean postContactValidationBean() {
        final ContactBean contactBean = new ContactBean();
        contactBean.setName(firstName + " " + lastName);
        contactBean.setEmail(getEmail());
        return contactBean;
    }
}
