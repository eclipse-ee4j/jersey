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

package org.glassfish.jersey.tests.integration.servlet_25_mvc_1.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.server.mvc.Template;

@Template
@Produces("text/html")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Item {

    private String title;
    private String author;

    public Item() {
    }

    public Item(final String title, final String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public Item getXml() {
        return this;
    }

    @Override
    public String toString() {
        return "Item{"
                + "title='" + title + '\''
                + ", author='" + author + '\''
                + '}';
    }
}
