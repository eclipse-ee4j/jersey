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

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Restricted entity to demonstrate various security annotations.
 *
 * @author Michal Gajdos
 */
@XmlRootElement
public class RestrictedEntity {

    private String simpleField;

    private String denyAll;

    private String permitAll;

    private RestrictedSubEntity mixedField;

    public String getSimpleField() {
        return simpleField;
    }

    @DenyAll
    public String getDenyAll() {
        return denyAll;
    }

    @PermitAll
    public String getPermitAll() {
        return permitAll;
    }

    @RolesAllowed({"manager", "user"})
    public RestrictedSubEntity getMixedField() {
        return mixedField;
    }

    public void setSimpleField(final String simpleField) {
        this.simpleField = simpleField;
    }

    public void setDenyAll(final String denyAll) {
        this.denyAll = denyAll;
    }

    public void setPermitAll(final String permitAll) {
        this.permitAll = permitAll;
    }

    public void setMixedField(final RestrictedSubEntity mixedField) {
        this.mixedField = mixedField;
    }

    /**
     * Get an instance of RestrictedEntity. This method creates always a new instance of RestrictedEntity.
     *
     * @return an instance of RestrictedEntity.
     */
    public static RestrictedEntity instance() {
        final RestrictedEntity entity = new RestrictedEntity();

        entity.setSimpleField("Simple Field.");
        entity.setDenyAll("Deny All.");
        entity.setPermitAll("Permit All.");

        final RestrictedSubEntity mixedField = new RestrictedSubEntity();
        mixedField.setManagerField("Manager's Field.");
        mixedField.setUserField("User's Field.");

        entity.setMixedField(mixedField);

        return entity;
    }
}
