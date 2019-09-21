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

package org.glassfish.jersey.tests.e2e.entity;

import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Paul Sandoz
 */
@XmlType(name = "dada")
public class JaxbBeanType {

    public String value;

    public JaxbBeanType() {
    }

    public JaxbBeanType(String str) {
        value = str;
    }

    public boolean equals(Object o) {
        if (!(o instanceof JaxbBeanType)) {
            return false;
        }
        return ((JaxbBeanType) o).value.equals(value);
    }

    public String toString() {
        return "JaxbBeanType: " + value;
    }
}
