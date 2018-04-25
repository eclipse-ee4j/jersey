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

package org.glassfish.jersey.tests.e2e.server.validation.validateonexecution;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.tests.e2e.server.validation.Extended;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Michal Gajdos
 */
@XmlRootElement
public class AnotherContactBean implements Serializable {

    private String email;

    private String phone;

    @NotBlank
    private String name;

    @NotNull(groups = {Extended.class})
    private String city;

    public AnotherContactBean() {
    }

    public AnotherContactBean(final String email, final String phone, final String name, final String city) {
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.city = city;
    }

    @Email(regexp = "[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")
    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Pattern(regexp = "[0-9]{3,9}")
    @ValidateOnExecution(type = ExecutableType.NON_GETTER_METHODS)
    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnotherContactBean)) {
            return false;
        }

        final AnotherContactBean that = (AnotherContactBean) o;

        if (email != null ? !email.equals(that.email) : that.email != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (phone != null ? !phone.equals(that.phone) : that.phone != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = email != null ? email.hashCode() : 0;
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AnotherContactBean{"
                + "email='" + email + '\''
                + ", phone='" + phone + '\''
                + ", name='" + name + '\''
                + '}';
    }
}
