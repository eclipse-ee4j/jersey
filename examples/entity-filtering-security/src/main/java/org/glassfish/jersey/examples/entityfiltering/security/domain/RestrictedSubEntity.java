/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.entityfiltering.security.domain;

import javax.annotation.security.RolesAllowed;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Restricted sub-entity to demonstrate that security entity-filtering is transitive.
 *
 * @author Michal Gajdos
 */
@XmlRootElement
public class RestrictedSubEntity {

    private String managerField;

    private String userField;

    @RolesAllowed("manager")
    public String getManagerField() {
        return managerField;
    }

    @RolesAllowed("user")
    public String getUserField() {
        return userField;
    }

    public void setManagerField(final String managerField) {
        this.managerField = managerField;
    }

    public void setUserField(final String userField) {
        this.userField = userField;
    }
}
