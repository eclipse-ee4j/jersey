/*
 * Copyright (c) 2012, 2022 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Custom logging filter.
 *
 * @author Santiago Pericas-Geertsen (santiago.pericasgeertsen at oracle.com)
 */
public class CustomLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter,
        ClientRequestFilter, ClientResponseFilter {

    static int preFilterCalled = 0;
    static int postFilterCalled = 0;

    @Override
    public void filter(ClientRequestContext context) throws IOException {
        System.out.println("CustomLoggingFilter.preFilter called");
        assertEquals("bar", context.getConfiguration().getProperty("foo"));
        preFilterCalled++;
    }

    @Override
    public void filter(ClientRequestContext context, ClientResponseContext clientResponseContext) throws IOException {
        System.out.println("CustomLoggingFilter.postFilter called");
        assertEquals("bar", context.getConfiguration().getProperty("foo"));
        postFilterCalled++;
    }

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        System.out.println("CustomLoggingFilter.preFilter called");
        assertEquals("bar", context.getProperty("foo"));
        preFilterCalled++;
    }

    @Override
    public void filter(ContainerRequestContext context, ContainerResponseContext containerResponseContext) throws IOException {
        System.out.println("CustomLoggingFilter.postFilter called");
        assertEquals("bar", context.getProperty("foo"));
        postFilterCalled++;
    }
}

