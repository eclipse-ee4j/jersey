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

/**
 * Nested testing bean, contained in {@link org.glassfish.jersey.tests.performance.tools.TestBean}
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class TestBeanInfo {

    @GenerateForTest
    public Integer someNumber;
    @GenerateForTest
    public int i; // same name as in the main bean, on purpose
    @GenerateForTest
    public TestBeanCoordinates coords; // another nested level

    public String printContent(int level) {
        String pad = String.format("%1$" + level + "s", "");
        StringBuffer buf = new StringBuffer();
        buf.append(pad + "# " + this).append("\n");
        buf.append(pad + "someNumber=" + someNumber).append("\n");
        buf.append(pad + "i=" + i).append("\n");
        if (coords != null) {
            buf.append(coords.printContent(level + 1));
        }
        return buf.toString();
    }
}
