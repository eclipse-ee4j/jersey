/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.example.bookshop.microprofile.client;

import org.glassfish.jersey.example.bookshop.microprofile.ressources.BookingInfo;
import org.glassfish.jersey.example.bookshop.microprofile.server.BookingFeatures;
import org.glassfish.jersey.example.bookshop.microprofile.server.LibraryFeatures;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to generate library customer.
 * Each thread represent a customer, you can change the number of customer under argument tag in pom.xml.
 *
 * Customers are going to reserve a book at the library so they contact the booking service though
 * the microprofile rest client.
 * If the client is not a library customer, he has to register at the library and then reserve wished book.
 *
 * Customer are defined by their name. They can reserve a book by giving their name, book's name
 * and the due date when they want to keep the book.
 */
public class LibraryClient {

    /**
     * Logger used to print usefull information in the command prompt.
     */
    private static final Logger LOGGER = Logger.getLogger(LibraryClient.class.getName());
    private static final Level level = Level.INFO;

    /**
     * List of available customer name
     */
    private static final List<String> customerNameList = Arrays.asList("Oliver", "Jhon", "Will", "Harry",
            "Cameron", "David", "Michael", "Stephen", "Robert", "George");

    /**
     * List of available book name
     */
    private static final List<String> bookNameList = Arrays.asList("Harry Potter", "Star Wars", "Games of Thrones",
            "Merlin", "Lord of the rings", "Spider-man", "Prince of Persia", "Pirate of Caribbean", "Hulk", "Iron man");

    /**
     * Start customer process
     * @param args number of customer
     */
    public static void main(String[] args) {

        int maxThread = Integer.parseInt(args[0]);
        LOGGER.setLevel(level);

        for (int i = 0; i < maxThread; i++){
            ClientThread ct = new ClientThread();
            ct.start();
        }
    }

    /**
     * Each thread create rest client to communicate with the server and reserve their book.
     */
    private static class ClientThread extends Thread {

        public void run() {
            try {

                final String customerName = randomCustomerName();
                final String bookName = randomBookName();

                System.out.println("Thread with customer " + customerName + " started !");

                BookingFeatures bookingClient = RestClientBuilder.newBuilder()
                        .baseUri(new URI("http://localhost:8080/booking"))
                        .build(BookingFeatures.class);

                ArrayList<Date> borrowingPeriod = randomDateArray();

                BookingInfo bookingInfo = new BookingInfo(
                        customerName,
                        bookName,
                        borrowingPeriod.get(0),
                        borrowingPeriod.get(1));

                Response response = bookingClient.reserveBookByName(bookingInfo);

                String bookingFeedback = response.readEntity(String.class);
                LOGGER.log(level, "Customer " + customerName + " : " + bookingFeedback);

                if (bookingFeedback.equals(customerName + " is not a customer of the library")) {
                    LibraryFeatures libraryClient = RestClientBuilder.newBuilder()
                            .baseUri(new URI("http://localhost:8080/library"))
                            .build(LibraryFeatures.class);

                    Response response1 = libraryClient.registerCustomer(customerName);
                    LOGGER.log(level, "Customer " + customerName + " : " + response1.readEntity(String.class));

                    Response response2 = bookingClient.reserveBookByName(bookingInfo);
                    LOGGER.log(level, "Customer " + customerName + " : " + response2.readEntity(String.class));
                }
                LOGGER.log(level, "Customer " + customerName + " : Finished the book reservation. \n Close Thread.");

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Choose a random customer name inside the list customerNameList.
     * @return Customer name as a String.
     */
    private static String randomCustomerName(){
        int random_int = (int) (Math.random() * 10);
        return customerNameList.get(random_int);
    }

    /**
     * Choose a random book name inside the list bookNameList.
     * @return Book's name as a String.
     */
    private static String randomBookName(){
        int randomPosition = (int) (Math.random() * 10);
        return bookNameList.get(randomPosition);
    }

    /**
     * Choose a random due date as Date over July month.
     * @return due date as ArrayList<Date>.
     */
    private static ArrayList<Date> randomDateArray(){
        Calendar calendar = Calendar.getInstance();

        int randomDay = (int) (Math.random() * 31);
        calendar.set(2020, Calendar.JULY, randomDay, 0, 0, 0);
        Date fromDate = calendar.getTime();

        randomDay = (int) (Math.random() * (31 - randomDay + 1) - randomDay);
        calendar.set(2020, Calendar.JULY, randomDay, 0, 0, 0);
        Date toDate = calendar.getTime();

        ArrayList<Date> history = new ArrayList<Date>();
        history.add(fromDate);
        history.add(toDate);

        return history;
    }

}
