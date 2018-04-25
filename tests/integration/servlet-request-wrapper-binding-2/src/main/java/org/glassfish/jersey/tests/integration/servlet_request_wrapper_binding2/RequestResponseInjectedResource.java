/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_request_wrapper_binding2;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 * Test resource that gets injected with the actual {@link HttpServletRequest} and
 * {@link HttpServletResponse} instance, so that we could testify custom implementations
 * has been used there.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("/")
public class RequestResponseInjectedResource {

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @GET
    @Path("requestType")
    public String getRequestType() throws Exception {

        // make sure we can access underlying request from another thread
        return executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return ((HttpServletRequestWrapper) request).getRequest().getClass().getName();
            }
        }).get();
    }

    @GET
    @Path("responseType")
    public String getResponseType() {

        return ((HttpServletResponseWrapper) response).getResponse().getClass().getName();
    }
}
