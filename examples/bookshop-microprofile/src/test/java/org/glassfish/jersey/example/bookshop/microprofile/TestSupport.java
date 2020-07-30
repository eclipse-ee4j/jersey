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

import org.glassfish.jersey.example.bookshop.microprofile.server.BookingService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import javax.ws.rs.core.Application;

public abstract class TestSupport extends JerseyTest {

    @Override
    protected Application configure() {
        ResourceConfig application = new ResourceConfig();
        application.packages(BookingService.class.getPackage().getName());
        set(TestProperties.CONTAINER_PORT, 8080);
        return application;
    }

}
