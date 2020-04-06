/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * SPDX-License-Identifier: BSD-2-Clause
 */

package org.glassfish.jersey.examples.bookstore.webapp.resource;

import java.util.Map;
import java.util.TreeMap;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.inject.Singleton;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.server.mvc.Template;

@Path("/")
@Singleton
@Template
@Produces(MediaType.TEXT_HTML)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Bookstore {

    private final Map<String, Item> items = new TreeMap<>();
    private String name;

    public Bookstore() {
        setName("Czech Bookstore");
        getItems().put("1", new Book("Svejk", "Jaroslav Hasek"));
        getItems().put("2", new Book("Krakatit", "Karel Capek"));
        getItems().put("3", new CD("Ma Vlast 1", "Bedrich Smetana", new Track[] {
                new Track("Vysehrad", 180),
                new Track("Vltava", 172),
                new Track("Sarka", 32)}));
    }

    @Path("items/{itemid}/")
    public Item getItem(@PathParam("itemid") String itemid) {
        Item i = getItems().get(itemid);
        if (i == null) {
            throw new NotFoundException(Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("Item, " + itemid + ", is not found")
                    .build());
        }

        return i;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public Bookstore getXml() {
        return this;
    }

    public long getSystemTime() {
        return System.currentTimeMillis();
    }

    public Map<String, Item> getItems() {
        return items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
