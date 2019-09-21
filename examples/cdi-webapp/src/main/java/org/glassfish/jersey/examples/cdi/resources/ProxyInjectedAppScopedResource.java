/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.cdi.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * Application scoped CDI bean to demonstrate a dynamic proxy is being injected
 * for URI info so that actual request information is available.
 *
 * @author Jakub Podlesak (jakub.podlesak at oralcle.com)
 */
@ApplicationScoped
@Path("ui-app")
public class ProxyInjectedAppScopedResource {

    @Context UriInfo uiField;

    @Path("{p}")
    @GET
    public String getUri(@Context UriInfo uiParam) {
        if (uiParam == uiField) {
            throw new IllegalStateException("Dynamic proxy expected in the uiField");
        }
        return uiField.getRequestUri().getPath();
    }
}
