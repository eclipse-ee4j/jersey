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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings("RedundantIfStatement")
@XmlRootElement
public class NullStringBean {

    @XmlElement(nillable = true)
    public String nullString = "not null to test if set to null works";

    public static Object createTestInstance() {
        return new NullStringBean();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NullStringBean other = (NullStringBean) obj;
        if ((this.nullString == null) ? (other.nullString != null) : !this.nullString.equals(other.nullString)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (this.nullString != null ? this.nullString.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{nullString:%s}", quoteDelimitedStringOrNull(nullString));
    }

    private String quoteDelimitedStringOrNull(String string) {
        return (string == null) ? "null" : String.format("\"%s\"", string);
    }
}
