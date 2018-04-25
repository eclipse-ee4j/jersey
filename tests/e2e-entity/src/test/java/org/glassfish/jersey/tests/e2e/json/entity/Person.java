/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.glassfish.jersey.tests.e2e.json.JsonTestHelper;

/**
 * @author Jay Feenan (jay.feenan at oracle.com)
 * @author Michal Gajdos
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement(name = "person")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name", "children"})
public class Person {

    public static Object createTestInstance() {
        Person daughter = new Person();
        daughter.setName("Jill Schmo");

        Person son = new Person();
        son.setName("Jack Schmo");

        Person person = new Person();
        person.setName("Joe Schmo");
        person.setChildren(new Person[]{daughter, son});

        return person;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Person other = (Person) obj;
        if ((this.m_name == null) ? (other.m_name != null) : !this.m_name.equals(other.m_name)) {
            return false;
        }
        return JsonTestHelper.areArraysEqual(m_children, other.m_children);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (this.m_name != null ? this.m_name.hashCode() : 0);
        hash = 19 * hash + (this.m_children != null ? Arrays.hashCode(this.m_children) : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{person: %s, %s}", m_name, Arrays.toString(m_children));
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    @XmlElementWrapper(name = "children")
    @XmlElement(name = "child")
    public Person[] getChildren() {
        return m_children;
    }

    public void setChildren(Person[] children) {
        m_children = children;
    }

    private String m_name;
    private Person[] m_children;
}
