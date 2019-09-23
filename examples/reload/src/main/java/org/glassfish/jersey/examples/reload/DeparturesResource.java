/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.reload;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author Jakub Podlesak
 */
@Path("departures")
public class DeparturesResource {

    @GET
    @Produces("text/plain")
    public String getDepartures() {
        FlightsDB.departuresReqCount.incrementAndGet();
        return "No departure scheduled in the following days";
    }
}
