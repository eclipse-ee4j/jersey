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
import java.util.Formatter;
import java.util.List;

import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.glassfish.jersey.tests.e2e.json.JsonTestHelper;

@SuppressWarnings({"UnusedDeclaration", "SimplifiableIfStatement", "StringEquality"})
@XmlRootElement()
@JsonbVisibility(CustomJsonbVisibilityStrategy.class)
public class AnotherArrayTestBean {

    public static Object createTestInstance() {
        AnotherArrayTestBean one = new AnotherArrayTestBean();
        AnotherCat c1 = new AnotherCat("Foo", "Kitty");
        one.addCat(c1);
        AnotherCat c2 = new AnotherCat("Bar", "Puss");
        one.addCat(c2);

        one.setProp("testProp");

        return one;
    }

    @XmlElement(required = true)
    protected List<AnotherCat> cats;
    protected String prop;

    public AnotherArrayTestBean() {
        this.cats = new ArrayList<>();
    }

    public void setCats(List<AnotherCat> cats) {
        this.cats = cats;
    }

    @JsonbTransient
    @XmlTransient
    public List<AnotherCat> getTheCats() {
        return this.cats;
    }

    public void addCat(AnotherCat c) {
        this.cats.add(c);
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnotherArrayTestBean other = (AnotherArrayTestBean) obj;
        if (this.prop != other.prop && (this.prop == null || !this.prop.equals(other.prop))) {
            return false;
        }
        return JsonTestHelper.areCollectionsEqual(cats, other.cats);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.prop != null ? this.prop.hashCode() : 0);
        hash = 79 * hash + (this.cats != null ? this.cats.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return (new Formatter()).format("AATB(a=%s, cd=%s)", prop, cats).toString();
    }
}
