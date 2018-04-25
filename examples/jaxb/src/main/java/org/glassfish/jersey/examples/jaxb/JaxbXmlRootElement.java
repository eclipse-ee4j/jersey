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

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JaxbXmlRootElement {

    public String value;

    public JaxbXmlRootElement() {
    }

    public JaxbXmlRootElement(String str) {
        value = str;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JaxbXmlRootElement)) {
            return false;
        }
        return ((JaxbXmlRootElement) o).value.equals(value);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}
