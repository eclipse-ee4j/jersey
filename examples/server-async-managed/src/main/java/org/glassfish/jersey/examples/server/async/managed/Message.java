/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.server.async.managed;

import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Chat message JAXB POJO.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@XmlRootElement
public class Message {

    public String author = "";
    public String message = "";
    public long time = new Date().getTime();

    public Message(String author, String message) {
        this.author = author;
        this.message = message;
    }

    public Message() {
    }

    @Override
    public String toString() {
        return "Message{" + "author=" + author + ", message=" + message + ", time=" + time + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Message other = (Message) obj;
        if ((this.author == null) ? (other.author != null) : !this.author.equals(other.author)) {
            return false;
        }
        if ((this.message == null) ? (other.message != null) : !this.message.equals(other.message)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.author != null ? this.author.hashCode() : 0);
        hash = 47 * hash + (this.message != null ? this.message.hashCode() : 0);
        return hash;
    }
}
