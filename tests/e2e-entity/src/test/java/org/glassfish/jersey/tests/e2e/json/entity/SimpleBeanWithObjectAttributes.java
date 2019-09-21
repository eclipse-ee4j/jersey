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

import java.net.URI;
import java.util.Formatter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Michal Gajdos
 */
@SuppressWarnings({"StringEquality", "RedundantIfStatement", "NumberEquality"})
@XmlRootElement
public class SimpleBeanWithObjectAttributes {

    @XmlAttribute
    public URI uri;
    public String s1;
    @XmlAttribute
    public Integer i;
    @XmlAttribute
    public String j;

    public SimpleBeanWithObjectAttributes() {
    }

    public static Object createTestInstance() {
        return new SimpleBeanWithObjectAttributes();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimpleBeanWithObjectAttributes other = (SimpleBeanWithObjectAttributes) obj;
        if (this.s1 != other.s1 && (this.s1 == null || !this.s1.equals(other.s1))) {
            return false;
        }
        if (this.j != other.j && (this.j == null || !this.j.equals(other.j))) {
            return false;
        }
        if (this.uri != other.uri && (this.uri == null || !this.uri.equals(other.uri))) {
            return false;
        }
        if (this.i != other.i && (this.i == null || !this.i.equals(other.i))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        if (null != s1) {
            hash += 17 * s1.hashCode();
        }
        if (null != j) {
            hash += 17 * j.hashCode();
        }
        if (null != uri) {
            hash += 17 * uri.hashCode();
        }
        hash += 13 * i;
        return hash;
    }

    @Override
    public String toString() {
        return (new Formatter()).format("SBWOA(%s,%d,%s,%s)", s1, i, j, uri).toString();
    }
}
