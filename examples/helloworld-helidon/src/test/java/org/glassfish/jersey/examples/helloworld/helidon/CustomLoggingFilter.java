/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.helidon;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Custom logging filter.
 *
 */
public class CustomLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter,
        ClientRequestFilter, ClientResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(CustomLoggingFilter.class.getName());

    static int preFilterCalled = 0;
    static int postFilterCalled = 0;

    @Override
    public void filter(ClientRequestContext context) throws IOException {
        LOGGER.info("CustomLoggingFilter.preFilter called");
        assertEquals("bar", context.getConfiguration().getProperty("foo"));
        preFilterCalled++;
    }

    @Override
    public void filter(ClientRequestContext context, ClientResponseContext clientResponseContext) throws IOException {
        LOGGER.info("CustomLoggingFilter.postFilter called");
        assertEquals("bar", context.getConfiguration().getProperty("foo"));
        postFilterCalled++;
    }

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        LOGGER.info("CustomLoggingFilter.preFilter called");
        assertEquals("bar", context.getProperty("foo"));
        preFilterCalled++;
    }

    @Override
    public void filter(ContainerRequestContext context, ContainerResponseContext containerResponseContext) throws IOException {
        LOGGER.info("CustomLoggingFilter.postFilter called");
        assertEquals("bar", context.getProperty("foo"));
        postFilterCalled++;
    }
}

