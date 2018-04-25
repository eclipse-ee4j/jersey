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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@SuppressWarnings("RedundantIfStatement")
@XmlRootElement
public class SimpleBeanWithJustOneAttribute {

    @XmlAttribute
    public URI uri;

    public SimpleBeanWithJustOneAttribute() {
    }

    public static Object createTestInstance() {
        SimpleBeanWithJustOneAttribute instance = new SimpleBeanWithJustOneAttribute();

        try {
            instance.uri = new URI("http://localhost:8080/jedna/bedna/");
        } catch (URISyntaxException ex) {
            Logger.getLogger(SimpleBeanWithJustOneAttribute.class.getName()).log(Level.SEVERE, null, ex);
        }

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
        final SimpleBeanWithJustOneAttribute other = (SimpleBeanWithJustOneAttribute) obj;
        if (this.uri != other.uri && (this.uri == null || !this.uri.equals(other.uri))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        if (null != uri) {
            hash += 17 * uri.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        return String.format("SBWJOA(%s)", uri);
    }
}
