/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.connector;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import static org.junit.Assert.assertEquals;

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
        assertEquals(context.getConfiguration().getProperty("foo"), "bar");
        preFilterCalled++;
    }

    @Override
    public void filter(ClientRequestContext context, ClientResponseContext clientResponseContext) throws IOException {
        System.out.println("CustomLoggingFilter.postFilter called");
        assertEquals(context.getConfiguration().getProperty("foo"), "bar");
        postFilterCalled++;
    }

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        System.out.println("CustomLoggingFilter.preFilter called");
        assertEquals(context.getProperty("foo"), "bar");
        preFilterCalled++;
    }

    @Override
    public void filter(ContainerRequestContext context, ContainerResponseContext containerResponseContext) throws IOException {
        System.out.println("CustomLoggingFilter.postFilter called");
        assertEquals(context.getProperty("foo"), "bar");
        postFilterCalled++;
    }
}
