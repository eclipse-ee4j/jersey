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

package org.glassfish.jersey.tests.e2e.entity.filtering.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.glassfish.jersey.tests.e2e.entity.filtering.PrimaryDetailedView;
import org.glassfish.jersey.tests.e2e.entity.filtering.SecondaryDetailedView;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Mixed views.
 *
 * @author Michal Gajdos
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.ANY)
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ComplexSubSubEntity {

    public static final ComplexSubSubEntity INSTANCE;

    static {
        INSTANCE = new ComplexSubSubEntity();
        INSTANCE.field = "field";
        INSTANCE.property = "property";
    }

    @XmlElement
    @SecondaryDetailedView
    public String field;

    private String property;

    @XmlTransient
    @JsonIgnore
    public String accessorTransient;

    public String getProperty() {
        return property;
    }

    public void setProperty(final String property) {
        this.property = property;
    }

    @PrimaryDetailedView
    public String getAccessor() {
        return accessorTransient == null ? field + field : accessorTransient;
    }

    public void setAccessor(final String accessor) {
        accessorTransient = accessor;
    }
}
