/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jaxb;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class JaxbXmlType {

    public String value;

    public JaxbXmlType() {
    }

    public JaxbXmlType(String str) {
        value = str;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JaxbXmlType)) {
            return false;
        }
        return ((JaxbXmlType) o).value.equals(value);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}
