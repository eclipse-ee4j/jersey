/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.benchmark.server.jnh;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Path("HttpMethod")
public class JNHttpMethodResource {

    private final static int BOOK_LIST_SIZE=1000;

    private final static List<Book> bookList = generateBooks();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getStatus() {
        return HttpStatus.OK_200.getReasonPhrase();
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Path("books")
    public List<Book> getBooks() {
        return bookList;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Path("book")
    public Book getBook() {
        return new Book("1", "2", 3);
    }

    @POST
    @Path("postBook")
    public void postBook(Book book) {
        /*System.out.println(String.format("Just got a book by: %s, which is called: %s, giving it an id: %d",
                book.getAuthor(),
                book.getTitle(),
                book.getId()));*/
    }

    @PUT
    @Path("putBook")
    public void putBook(Book book) {
        /*System.out.println(String.format("A book by: %s, with name: %s, is putted back under the id: %d",
                book.getAuthor(),
                book.getTitle(),
                book.getId()));*/
    }

    @DELETE
    @Path("deleteBook")
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteBook() {
        return "HttpStatus.OK_200";

    }

    private static final List<Book> generateBooks() {
        final List<Book> books = new ArrayList<>();
        for (int i = 0; i < BOOK_LIST_SIZE; i++) {
            books.add(new Book("Title: " + i, "Author: " + i, i));
        }
        return books;
    }
}
