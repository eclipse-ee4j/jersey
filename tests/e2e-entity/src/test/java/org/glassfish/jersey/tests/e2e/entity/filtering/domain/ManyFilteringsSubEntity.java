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

import org.glassfish.jersey.tests.e2e.entity.filtering.PrimaryDetailedView;
import org.glassfish.jersey.tests.e2e.entity.filtering.SecondaryDetailedView;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
* @author Michal Gajdos
*/
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.ANY)
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ManyFilteringsSubEntity {

    public static final ManyFilteringsSubEntity INSTANCE;

    static {
        INSTANCE = new ManyFilteringsSubEntity();
        INSTANCE.field1 = 60;
        INSTANCE.field2 = 70;
        INSTANCE.property1 = "property1";
        INSTANCE.property2 = "property2";
    }

    @XmlElement
    public int field1;

    @XmlElement
    @SecondaryDetailedView
    public int field2;

    private String property1;
    private String property2;

    @PrimaryDetailedView
    public String getProperty1() {
        return property1;
    }

    public void setProperty1(final String property1) {
        this.property1 = property1;
    }

    @SecondaryDetailedView
    public String getProperty2() {
        return property2;
    }

    @PrimaryDetailedView
    public void setProperty2(final String property2) {
        this.property2 = property2;
    }
}
