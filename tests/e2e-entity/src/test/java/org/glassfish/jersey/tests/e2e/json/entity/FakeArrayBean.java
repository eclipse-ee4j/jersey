/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.json.entity;

import java.util.ArrayList;
import java.util.List;

import javax.json.bind.annotation.JsonbVisibility;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Test case for issue#310.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings({"RedundantIfStatement", "UnusedDeclaration"})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "weight", "color", "name"
})
@XmlRootElement()
@JsonbVisibility(CustomJsonbVisibilityStrategy.class)
public class FakeArrayBean {

    protected List<String> weight;
    @XmlElement(required = true)
    protected String color;
    @XmlElement(required = true)
    protected String name;

    public List<String> getWeight() {
        if (weight == null) {
            weight = new ArrayList<>();
        }
        return this.weight;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String value) {
        this.color = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public static Object createTestInstance() {
        FakeArrayBean result = new FakeArrayBean();

        result.getWeight().add("1kg");
        result.getWeight().add("2kg");
        result.setColor("red");
        result.setName("bumper");

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FakeArrayBean other = (FakeArrayBean) obj;
        if (this.weight != other.weight && (this.weight == null || !this.weight.equals(other.weight))) {
            return false;
        }
        if ((this.color == null) ? (other.color != null) : !this.color.equals(other.color)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.weight != null ? this.weight.hashCode() : 0);
        hash = 29 * hash + (this.color != null ? this.color.hashCode() : 0);
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{name:%s, color:%s, weights:%s}", name, color, weight);
    }
}
