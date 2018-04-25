/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * CDI based JAX-RS interceptor that re-writes the original output
 * with request ID obtained from another CDI bean.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@ResponseBodyFromCdiBean
@ApplicationScoped
public class CustomInterceptor implements WriterInterceptor {

    // CDI injected proxy
    @Inject RequestScopedBean bean;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        final String requestId = bean.getRequestId();
        context.getOutputStream().write(requestId.getBytes());
    }
}
