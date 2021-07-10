/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.library.microprofile.server;

import org.glassfish.jersey.examples.library.microprofile.ressources.Book;
import org.glassfish.jersey.examples.library.microprofile.services.LibraryService;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Library Service manage books and the list of registered customer.
 *
 * Clients can register at the library, by default, Harry is the only registered customer.
 * They can check if the book is part of the library as well.
 *
 * Library service can be reached at http://localhost:8080/library.
 */
@Path("/library")
public class LibraryResource implements LibraryService {

    private static ArrayList<Book> bookList = createBookList();

    private static ArrayList<String> customerList = new ArrayList<String>(Collections.singletonList("Harry"));

    /**
     * Fulfill the library
     *
     * @return ArrayList of book
     */
    private static ArrayList<Book> createBookList(){
        ArrayList<Book> localBookList = new ArrayList<Book>();
        localBookList.add(new Book("Harry Potter", 100));
        localBookList.add(new Book("Star Wars", 100));
        localBookList.add(new Book("Games of Thrones", 100));
        localBookList.add(new Book("Merlin", 100));
        localBookList.add(new Book("Lord of the rings", 100));
        localBookList.add(new Book("Spider-man", 100));
        localBookList.add(new Book("Prince of Persia", 100));
        localBookList.add(new Book("Pirate of Caribbean", 100));
        localBookList.add(new Book("Hulk", 100));
        localBookList.add(new Book("Iron man", 100));
        return localBookList;
    }

    /**
     * Check if the customer is registered at the library.
     *
     * @param customerName Customer's name.
     * @return             Return true if the customer is registered and false if he is not.
     */
    @Override
    public boolean isRegistered(String customerName) {
        return customerList.contains(customerName);
    }

    /**
     * Check if the book is at the library.
     *
     * @param bookName  Book's name.
     * @return          Return true if the book is at the library and false if it is not.
     */
    @Override
    public boolean containsBook(String bookName) {
        for (Book b : bookList) {
            if (b.getName().equals(bookName)){
                return true;
            }
        }
        return false;
    }

    /**
     * Register a new customer at the library.
     *
     * @param customerName  Customer's name.
     * @return              Return Response with registration confirmation.
     */
    @Override
    public Response registerCustomer(String customerName) {
        customerList.add(customerName);
        return Response.ok().entity("Customer " + customerName + " successfully registered at the library").build();
    }

}
