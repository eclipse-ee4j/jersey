/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Example of a complex testing bean.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@XmlRootElement
public class TestBean {

    /* primitives */
    @GenerateForTest
    public byte bt;
    @GenerateForTest
    public short sh;
    @GenerateForTest
    public int i;
    @GenerateForTest
    public long l;
    @GenerateForTest
    public float f;
    @GenerateForTest
    public double d;
    @GenerateForTest
    public boolean bl;
    @GenerateForTest
    public char c;

    /* primitive wrappers */
    @GenerateForTest
    public Byte wrapBt;
    @GenerateForTest
    public Short wrapSh;
    @GenerateForTest
    public Integer wrapI;
    @GenerateForTest
    public Long wrapL;
    @GenerateForTest
    public Float wrapF;
    @GenerateForTest
    public Double wrapD;
    @GenerateForTest
    public Boolean wrapBl;
    @GenerateForTest
    public Character wrapC;

    /* arrays */
    @GenerateForTest(length = 7)
    public Integer[] array;

    @GenerateForTest
    public Date date;

    /* 1D - collections */
    @XmlElementWrapper(name = "StringElements")
    @XmlElement(name = "StringElement")
    @GenerateForTest(collectionMemberType = String.class, implementingClass = ArrayList.class, length = 10)
    public List<String> stringList;

    @XmlElementWrapper(name = "IntegerElements")
    @XmlElement(name = "IntegerElement")
    @GenerateForTest(collectionMemberType = Integer.class, length = 5)
    public HashSet<Integer> integerSet;

    /* enums */
    @GenerateForTest
    public TestBeanEnum en;

    /* custom types */
    @GenerateForTest
    public TestBeanInfo inner;

    /* recursive */
    @GenerateForTest
    public TestBean nextBean;

    /* and what about those? */
    // CharSequence cs;
    // Object o;
    // Map<String, String> map;

    @Override
    public String toString() {
        return printContent(0);
    }

    public String printContent(int level) {
        String pad = level == 0 ? "" : String.format("%1$" + level + "s", "");
        StringBuffer buf = new StringBuffer();
        buf.append(pad + "# TestBean[level=" + level + "]@" + Integer.toHexString(hashCode())).append("\n");

        buf.append(pad + "# Primitives").append("\n");
        buf.append(pad + "[" + bt + ", " + sh + ", " + i + ", " + l + ", " + f + ", " + d + ", " + bl + ", " + c + "]")
                .append("\n");

        buf.append(pad + "# Primitives wrappers").append("\n");
        buf.append(pad + "[" + wrapBt + ", " + wrapSh + ", " + wrapI + ", " + wrapL + ", " + wrapF + ", " + wrapD + ", "
                + wrapBl + ", " + wrapC + "]").append("\n");

        buf.append(pad + "# Arrays").append("\n");
        if (array != null) {
            buf.append(pad + "array: ");
            for (Integer i : array) {
                buf.append(i + ", ");
            }
            buf.append("\n");
        }

        buf.append(pad + "# Collections").append("\n");
        if (stringList != null) {
            buf.append(pad + "stringList: ");
            for (String s : stringList) {
                buf.append(s + ", ");
            }
            buf.append("\n");
        }
        if (integerSet != null) {
            buf.append(pad + "integerSet: ");
            for (Integer i : integerSet) {
                buf.append(i + ", ");
            }
            buf.append("\n");
        }

        if (date != null) {
            buf.append(pad + "date: " + date).append("\n");
        }

        buf.append(pad + "# Enums").append("\n");
        if (en != null) {
            buf.append(pad + "en=" + en).append("\n");
        }
        buf.append(pad + "# Inner bean").append("\n");
        if (inner != null) {
            buf.append(inner.printContent(level + 1));
        }
        buf.append("\n");
        buf.append(pad + "# Recursive bean").append("\n");
        if (nextBean != null) {
            buf.append(nextBean.printContent(level + 1));
        }
        return buf.toString();
    }

}
