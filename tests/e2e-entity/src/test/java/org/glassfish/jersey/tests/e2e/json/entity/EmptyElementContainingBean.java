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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings("RedundantIfStatement")
@XmlRootElement
public class EmptyElementContainingBean {

    public EmptyElementBean emptyBean;
    public String c;
    public String d;

    public static Object createTestInstance() {
        EmptyElementContainingBean result = new EmptyElementContainingBean();
        result.c = "foo";
        result.d = "bar";
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
        final EmptyElementContainingBean other = (EmptyElementContainingBean) obj;
        if (this.emptyBean != other.emptyBean && (this.emptyBean == null || !this.emptyBean.equals(other.emptyBean))) {
            return false;
        }
        if ((this.c == null) ? (other.c != null) : !this.c.equals(other.c)) {
            return false;
        }
        if ((this.d == null) ? (other.d != null) : !this.d.equals(other.d)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.emptyBean != null ? this.emptyBean.hashCode() : 0);
        hash = 59 * hash + (this.c != null ? this.c.hashCode() : 0);
        hash = 59 * hash + (this.d != null ? this.d.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{c:%s,d:%s}", c, d);
    }

}
