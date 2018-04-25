/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jersey_ejb.entities;

import java.util.Date;

/**
 * Message bean representing a single message.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class Message {

    private Date created;
    private String message;
    private int uniqueId;

    public Message(Date created, String message, int uniqueId) {
        this.created = created;
        this.message = message;
        this.uniqueId = uniqueId;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    @Override
    public String toString() {
        return "<span class='created'>CREATED: " + created + "</span> <span class='uniqueId'>ID: " + uniqueId
                + "</span> <span class='message'>MESSAGE: " + message + "</span>";
    }
}
