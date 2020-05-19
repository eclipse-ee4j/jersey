/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.cdi.resources;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

/**
 * Request scoped CDI bean to demonstrate no dynamic proxy is being injected
 * for JAX-RS request scoped URI info.
 *
 * @author Jakub Podlesak (jakub.podlesak at oralcle.com)
 */
@RequestScoped
@Path("ui-req")
public class RequestScopedResource {

    @Context UriInfo uiField;

    @Path("{p}")
    @GET
    public String getUri(@Context UriInfo uiParam) {
        if (uiParam != uiField) {
            throw new IllegalStateException("No dynamic proxy expected in the uiField");
        }
        return uiField.getRequestUri().getPath();
    }
}
