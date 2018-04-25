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

import javax.json.bind.annotation.JsonbVisibility;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.tests.e2e.json.JsonTestHelper;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@XmlRootElement
@JsonbVisibility(CustomJsonbVisibilityStrategy.class)
public class ComplexBeanWithAttributes3 {

    @XmlElement
    SimpleBeanWithJustOneAttribute b, c;

    public static Object createTestInstance() {
        ComplexBeanWithAttributes3 instance = new ComplexBeanWithAttributes3();
        instance.b = JsonTestHelper.createTestInstance(SimpleBeanWithJustOneAttribute.class);
        instance.c = JsonTestHelper.createTestInstance(SimpleBeanWithJustOneAttribute.class);
        return instance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComplexBeanWithAttributes3)) {
            return false;
        }
        final ComplexBeanWithAttributes3 other = (ComplexBeanWithAttributes3) obj;
        if (this.b != other.b && (this.b == null || !this.b.equals(other.b))) {
            System.out.println("b differs");
            return false;
        }
        if (this.c != other.c && (this.c == null || !this.c.equals(other.c))) {
            System.out.println("c differs");
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (this.b != null ? this.b.hashCode() : 0);
        hash = 19 * hash + (this.c != null ? this.c.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("CBWA2(%s,%s)", b, c);
    }
}
