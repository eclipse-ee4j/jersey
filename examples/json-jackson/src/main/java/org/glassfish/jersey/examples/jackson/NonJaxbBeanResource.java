/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jackson;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.server.JSONP;

/**
 * @author Jakub Podlesak
 */
@Path("/nonJaxbResource")
public class NonJaxbBeanResource {

    @GET
    @JSONP
    @Produces({"application/javascript", MediaType.APPLICATION_JSON})
    public NonJaxbBean getSimpleBeanJSONP() {
        return new NonJaxbBean();
    }
}
