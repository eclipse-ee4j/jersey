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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Doug Kohlert
 */
@XmlRootElement(name = "jaxbBean")
public class JaxbBean {

    public String value;

    public JaxbBean() {
    }

    public JaxbBean(String str) {
        value = str;
    }

    public boolean equals(Object o) {
        if (!(o instanceof JaxbBean)) {
            return false;
        }
        return ((JaxbBean) o).value.equals(value);
    }

    public String toString() {
        return "JAXBClass: " + value;
    }
}
