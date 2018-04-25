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

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import javax.json.bind.annotation.JsonbVisibility;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.tests.e2e.json.JsonTestHelper;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings({"StringEquality", "RedundantIfStatement"})
@XmlRootElement
@JsonbVisibility(CustomJsonbVisibilityStrategy.class)
public class ComplexBeanWithAttributes {

    @XmlAttribute
    public String a1;
    @XmlAttribute
    public int a2;
    @XmlElement
    public String filler1;
    @XmlElement
    public List<SimpleBeanWithAttributes> list;
    @XmlElement
    public String filler2;
    @XmlElement
    SimpleBeanWithAttributes b;

    public static Object createTestInstance() {
        ComplexBeanWithAttributes instance = new ComplexBeanWithAttributes();
        instance.a1 = "hello dolly";
        instance.a2 = 31415926;
        instance.filler1 = "111";
        instance.filler2 = "222";
        instance.b = JsonTestHelper.createTestInstance(SimpleBeanWithAttributes.class);
        instance.list = new LinkedList<>();
        instance.list.add(JsonTestHelper.createTestInstance(SimpleBeanWithAttributes.class));
        instance.list.add(JsonTestHelper.createTestInstance(SimpleBeanWithAttributes.class));
        return instance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComplexBeanWithAttributes)) {
            return false;
        }
        final ComplexBeanWithAttributes other = (ComplexBeanWithAttributes) obj;
        if (this.a1 != other.a1 && (this.a1 == null || !this.a1.equals(other.a1))) {
            return false;
        }
        if (this.a2 != other.a2) {
            return false;
        }
        if (this.b != other.b && (this.b == null || !this.b.equals(other.b))) {
            return false;
        }
        if (this.filler1 != other.filler1 && (this.filler1 == null || !this.filler1.equals(other.filler1))) {
            return false;
        }
        if (this.filler2 != other.filler2 && (this.filler2 == null || !this.filler2.equals(other.filler2))) {
            return false;
        }
        if (this.list != other.list && (this.list == null || !this.list.equals(other.list))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (this.a1 != null ? this.a1.hashCode() : 0);
        hash = 19 * hash + this.a2;
        hash = 19 * hash + (this.b != null ? this.b.hashCode() : 0);
        hash = 19 * hash + (this.filler1 != null ? this.filler1.hashCode() : 0);
        hash = 19 * hash + (this.filler2 != null ? this.filler2.hashCode() : 0);
        hash = 19 * hash + (this.list != null ? this.list.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return (new Formatter()).format("CBWA(%s,%d,%s)", a1, a2, b).toString();
    }
}
