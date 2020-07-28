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
import org.glassfish.jersey.example.bookshop.microprofile.server.LibraryFeatures;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class LibraryTest extends TestSupport {

    @Test
    public void registrationTest() throws URISyntaxException {
        LibraryFeatures libraryClient = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:8080/library"))
                .build(LibraryFeatures.class);

        assertTrue(libraryClient.isRegistered("Harry"));

        libraryClient.registerCustomer("Oliver");

        assertTrue(libraryClient.isRegistered("Oliver"));

    }

    @Test
    public void containsBookTest() throws URISyntaxException {
        LibraryFeatures libraryClient = RestClientBuilder.newBuilder()
                .baseUri(new URI("http://localhost:8080/library"))
                .build(LibraryFeatures.class);

        assertTrue(libraryClient.containsBook("Harry Potter"));
    }
}
