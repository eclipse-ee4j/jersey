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

import org.glassfish.jersey.tests.e2e.entity.filtering.TertiaryDetailedView;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author Michal Gajdos
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.ANY)
@XmlAccessorType(XmlAccessType.PROPERTY)
// Entity Filtering.
@TertiaryDetailedView
public class FilteredClassEntity {

    public static final FilteredClassEntity INSTANCE;

    static {
        INSTANCE = new FilteredClassEntity();
        INSTANCE.field = 40;
        INSTANCE.property = "property";
    }

    @XmlElement
    public int field;
    private String property;

    public String getProperty() {
        return property;
    }

    public void setProperty(final String property) {
        this.property = property;
    }
}
