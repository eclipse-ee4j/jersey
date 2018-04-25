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

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings("RedundantIfStatement")
@XmlRootElement(name = "item2")
public class TwoListsWrapperBean {

    public List<String> property1, property2;

    public static Object createTestInstance() {
        TwoListsWrapperBean instance = new TwoListsWrapperBean();
        instance.property1 = new LinkedList<String>();
        instance.property1.add("a1");
        instance.property1.add("a1");
        instance.property2 = new LinkedList<String>();
        instance.property2.add("b1");
        return instance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TwoListsWrapperBean other = (TwoListsWrapperBean) obj;
        if (this.property1 != other.property1 && (this.property1 == null || !this.property1.equals(other.property1))) {
            return false;
        }
        if (this.property2 != other.property2 && (this.property2 == null || !this.property2.equals(other.property2))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.property1 != null ? this.property1.hashCode() : 0);
        hash = 59 * hash + (this.property2 != null ? this.property2.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{twoListsWrapperBean:{property1:%s, property2:%s}}", property1, property2);
    }

}
