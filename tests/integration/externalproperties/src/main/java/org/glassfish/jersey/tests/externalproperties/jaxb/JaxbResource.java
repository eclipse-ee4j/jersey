/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.externalproperties.jaxb;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

@Path("library")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class JaxbResource {

    private static final HashMap<String, Book> bookList = new HashMap<>();

    @GET
    public String welcomeMessage() {
        return "Welcome to the Library";
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public String addBook(Book book){
        if (bookList.containsKey(book.getTitle())) {
            return "This book is already in the library";
        }
        bookList.put(book.getTitle(), book);
        return "Book added to the Library";
    }

    @GET
    @Path("{bookName}")
    @Produces(MediaType.APPLICATION_XML)
    public Book getBookByName(@PathParam("bookName") String bookName) throws Exception {
        if (bookList.containsKey(bookName)) {
            return bookList.get(bookName);
        }

        throw new Exception("Error: This book is not at the library");
    }

    @DELETE
    @Path("{bookName}")
    public String removeBookByName(@PathParam("bookName") String bookName) throws Exception {
        if (bookList.containsKey(bookName)) {
            bookList.remove(bookName);
            return bookName + " successfully removed from library";
        }

        throw new Exception("Error: This book is not at the library");
    }
}
