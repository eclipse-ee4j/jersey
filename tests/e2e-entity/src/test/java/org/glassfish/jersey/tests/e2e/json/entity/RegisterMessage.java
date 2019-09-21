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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author David Kaspar
 */
@SuppressWarnings("RedundantIfStatement")
@XmlRootElement
public final class RegisterMessage {

    @XmlAttribute
    public String agentUID;

    @XmlAttribute
    public long requestTime;

    public RegisterMessage() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RegisterMessage other = (RegisterMessage) obj;
        if ((this.agentUID == null) ? (other.agentUID != null) : !this.agentUID.equals(other.agentUID)) {
            return false;
        }
        if (this.requestTime != other.requestTime) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.agentUID != null ? this.agentUID.hashCode() : 0);
        hash = 89 * hash + (int) (this.requestTime ^ (this.requestTime >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{\"agentUID\":\"%s\", \"requestTime\":%d}", agentUID, requestTime);
    }

    public static Object createTestInstance() {
        RegisterMessage rm = new RegisterMessage();
        rm.agentUID = "agentKocka";
        rm.requestTime = 1234L;
        return rm;
    }

}
