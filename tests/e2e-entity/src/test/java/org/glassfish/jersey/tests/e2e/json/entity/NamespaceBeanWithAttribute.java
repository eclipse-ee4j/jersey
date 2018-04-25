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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings({"UnusedDeclaration", "RedundantIfStatement"})
@XmlRootElement
public class NamespaceBeanWithAttribute {

    @XmlAttribute(namespace = "http://example.com")
    public String attr;

    @XmlElement
    public String a;

    @XmlElement(namespace = "http://example.com")
    public String b;

    public NamespaceBeanWithAttribute() {
    }

    public NamespaceBeanWithAttribute(String attr, String a, String b) {
        this.attr = attr;
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NamespaceBeanWithAttribute other = (NamespaceBeanWithAttribute) obj;
        if ((this.attr == null) ? (other.attr != null) : !this.attr.equals(other.attr)) {
            return false;
        }
        if ((this.a == null) ? (other.a != null) : !this.a.equals(other.a)) {
            return false;
        }
        if ((this.b == null) ? (other.b != null) : !this.b.equals(other.b)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.attr != null ? this.attr.hashCode() : 0);
        hash = 37 * hash + (this.a != null ? this.a.hashCode() : 0);
        hash = 37 * hash + (this.b != null ? this.b.hashCode() : 0);
        return hash;
    }

    public static Object createTestInstance() {
        return new NamespaceBeanWithAttribute("value", "foo", "bar");
    }

    @Override
    public String toString() {
        return String.format("{attr:%s, a:%s, b:%s", attr, a, b);
    }

}
