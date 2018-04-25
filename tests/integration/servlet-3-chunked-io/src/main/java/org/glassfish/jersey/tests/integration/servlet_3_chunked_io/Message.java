/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_3_chunked_io;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Message POJO.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Message {

    /**
     * Message id.
     */
    public int id;
    /**
     * Message content.
     */
    public String data;

    /**
     * Create new message.
     */
    public Message() {
        this.id = -1;
        this.data = "";
    }

    /**
     * Create new message.
     * @param id message id.
     * @param data message content.
     */
    public Message(int id, String data) {
        if (data == null) {
            throw new IllegalArgumentException("Message data must not be null.");
        }

        this.id = id;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message message = (Message) o;

        if (id != message.id) {
            return false;
        }
        if (!data.equals(message.data)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "{\"id\":" + id + ",\"data\":\"" + data + "\"}";
    }
}
