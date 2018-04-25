/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jersey_ejb.resources;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ejb.Singleton;

import org.glassfish.jersey.examples.jersey_ejb.entities.Message;

/**
 * An EJB singleton to maintain all processed message beans.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Singleton
public class MessageHolderSingletonBean {

    private List<Message> list = new CopyOnWriteArrayList<Message>();
    private int maxMessages = 10;

    int currentId = 0;

    public MessageHolderSingletonBean() {
        // initial content
        addMessage("msg0", new Date(0));
        addMessage("msg1", new Date(1000));
        addMessage("msg2", new Date(2000));
    }

    public List<Message> getMessages() {
        List<Message> l = new LinkedList<Message>();

        int index = 0;

        while (index < list.size() && index < maxMessages) {
            l.add(list.get(index));
            index++;
        }

        return l;
    }

    private int getNewId() {
        return currentId++;
    }

    public Message addMessage(String msg) {
        return addMessage(msg, new Date());
    }

    private Message addMessage(String msg, Date date) {
        Message m = new Message(date, msg, getNewId());

        list.add(0, m);

        return m;
    }

    public Message getMessage(int uniqueId) {
        int index = 0;
        Message m;

        while (index < list.size()) {
            if ((m = list.get(index)).getUniqueId() == uniqueId) {
                return m;
            }
            index++;
        }

        return null;
    }

    public boolean deleteMessage(int uniqueId) {
        int index = 0;

        while (index < list.size()) {
            if (list.get(index).getUniqueId() == uniqueId) {
                list.remove(index);
                return true;
            }
            index++;
        }

        return false;
    }
}
