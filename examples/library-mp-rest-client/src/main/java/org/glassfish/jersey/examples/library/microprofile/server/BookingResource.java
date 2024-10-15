/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.library.microprofile.server;

import org.glassfish.jersey.examples.library.microprofile.ressources.BookingInfo;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.glassfish.jersey.examples.library.microprofile.services.BookingService;
import org.glassfish.jersey.examples.library.microprofile.services.LibraryService;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * The booking service manages the borrowingHistory of all book from the library.
 * It is in charge of checking book's availability as well as customer registration.
 *
 * Booking service can be found at http://localhost:8080/booking.
 */
@Path("/booking")
public class BookingResource implements BookingService {

    /**
     * The HashMap represent the timetable of all the book queried by customer.
     * It is updated every time a customer make a new reservation.
     * The key String is the name of the book which get an ArrayList containing all the period where the book is reserved.
     * Periods are defined by couples of Date = {fromDate ; toDate}, so the number of Date is always even.
     */
    private static HashMap<String, ArrayList<Date>> borrowingHistory = new HashMap<String, ArrayList<Date>>();

    @Inject
    @RestClient
    private LibraryService libraryClient;

    /**
     * Process book and customer check up.
     *
     * @param bookingInfo Contains reservation data from the client
     *                    customerName Customer name.
     *                    bookName     Book name.
     *                    fromDate     Picking up date of the book.
     *                    toDate       Return date of the book.
     * @return Status of the request as an entity inside the Response.
     */
    @Override
    public Response reserveBookByName(BookingInfo bookingInfo) {

        DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

        String customerName = bookingInfo.getCustomerName();
        String bookName = bookingInfo.getBookName();
        Date fromDate = bookingInfo.getFromDate();
        Date toDate = bookingInfo.getToDate();

        if (!libraryClient.containsBook(bookName)) {
            return Response.ok().entity(bookName + " is not at the library").build();
        }

        if (!libraryClient.isRegistered(customerName)) {
            return Response.ok().entity(customerName + " is not a customer of the library").build();
        }

        if (isBookAvailable(bookName, fromDate, toDate)) {
            addBorrowingDateToHistory(bookName, fromDate, toDate);
            return Response
                    .ok()
                    .entity("Book : " + bookName + " is successfully booked from "
                            + format.format(fromDate) + " to " + format.format(toDate))
                    .build();
        }

        return Response
                .ok()
                .entity("Book : " + bookName + " is already booked by someone else ... ")
                .build();
    }

    /**
     * Check in the borrowingHistory if the book is available for the required period of time.
     *
     * @param bookName  Book's name to be booked.
     * @param fromDate  Picking up date of the book.
     * @param toDate    Return date of the book.
     * @return          Return if the book can be borrowed for the required period of time.
     */
    private boolean isBookAvailable(String bookName, Date fromDate, Date toDate) {
        ArrayList<Date> dueDate = borrowingHistory.get(bookName);

        if (dueDate == null){
            return true;
        }

        for (int i = 0; i < (dueDate.size() / 2);  i++) {
            //Check if the booking is inside another booking planning
            if (fromDate.after(dueDate.get(i)) && fromDate.before(dueDate.get(i + 1))){
                return false;
            }
            if (toDate.after(dueDate.get(i)) && toDate.before(dueDate.get(i + 1))) {
                return false;
            }
            //Check if the booking date is around booking planning
            if (fromDate.before(dueDate.get(i)) && toDate.after(dueDate.get(i + 1))) {
                return false;
            }
            //Check if the booking dates are inside a booking planning
            if (fromDate.after(dueDate.get(i)) && toDate.before(dueDate.get(i + 1))) {
                return false;
            }
            if (fromDate.equals(dueDate.get(i)) || toDate.equals(dueDate.get(i + 1))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Complete the reservation by adding due date to the borrowingHistory.
     *
     * @param bookName  Name of the book to be booked.
     * @param fromDate  Picking up date of the book.
     * @param toDate    Return date of the book.
     */
    private void addBorrowingDateToHistory(String bookName, Date fromDate, Date toDate) {
        ArrayList<Date> history = borrowingHistory.get(bookName);

        if (history == null) {
            history = new ArrayList<Date>();
        }

        history.add(fromDate);
        history.add(toDate);
        borrowingHistory.put(bookName, history);
    }
}
