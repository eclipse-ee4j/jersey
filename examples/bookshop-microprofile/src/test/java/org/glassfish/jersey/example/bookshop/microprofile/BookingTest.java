/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.example.bookshop.microprofile;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.glassfish.jersey.example.bookshop.microprofile.ressources.BookingInfo;
import org.glassfish.jersey.example.bookshop.microprofile.server.BookingFeatures;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class BookingTest extends TestSupport {

    private final DateFormat format = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

    @Test
    public void reserveBookTwiceTest() throws URISyntaxException {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JULY, 2, 0, 0, 0);
        Date fromDate = calendar.getTime();

        calendar.set(2020, Calendar.JULY, 3, 0, 0, 0);
        Date toDate = calendar.getTime();

        BookingFeatures bookingClient = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:8080/booking"))
                .build(BookingFeatures.class);

        BookingInfo bookingInfo = new BookingInfo(
                "Harry",
                "Harry Potter",
                fromDate,
                toDate);

        Response response = bookingClient.reserveBookByName(bookingInfo);

        assertEquals("Book : Harry Potter is successfully booked from "
                + format.format(fromDate) + " to " + format.format(toDate), response.readEntity(String.class));

        Response response1 = bookingClient.reserveBookByName(bookingInfo);

        assertEquals("Book : Harry Potter is already booked by someone else ... ", response1.readEntity(String.class));

    }

    @Test
    public void reserveWrongBookName() throws URISyntaxException {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JULY, 1, 0, 0, 0);
        Date fromDate = calendar.getTime();

        calendar.set(2020, Calendar.JULY, 2, 0, 0, 0);
        Date toDate = calendar.getTime();

        BookingInfo bookingInfo = new BookingInfo(
                "Harry",
                "wrongName",
                fromDate,
                toDate);

        BookingFeatures bookingClient = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:8080/booking"))
                .build(BookingFeatures.class);

        Response response = bookingClient.reserveBookByName(bookingInfo);

        assertEquals("wrongName is not at the library", response.readEntity(String.class));
    }

    @Test
    public void reserveWrongCustomerName() throws URISyntaxException {

        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JULY, 1, 0, 0, 0);
        Date fromDate = calendar.getTime();

        calendar.set(2020, Calendar.JULY, 2, 0, 0, 0);
        Date toDate = calendar.getTime();

        BookingInfo bookingInfo = new BookingInfo(
                "wrongName",
                "Harry Potter",
                fromDate,
                toDate);

        BookingFeatures bookingClient = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:8080/booking"))
                .build(BookingFeatures.class);

        Response response = bookingClient.reserveBookByName(bookingInfo);

        assertEquals("wrongName is not a customer of the library", response.readEntity(String.class));
    }

}
