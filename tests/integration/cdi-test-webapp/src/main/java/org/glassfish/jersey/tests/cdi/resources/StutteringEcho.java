/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.enterprise.context.ApplicationScoped;

/**
 * Echo implementation to stutter given input n-times.
 * The stutter factor could be set via JAX-RS interface.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Stuttering
@ApplicationScoped
@Path("stutter-service-factor")
public class StutteringEcho implements EchoService {

    private static final int MIN_FACTOR = 2;
    private int factor = MIN_FACTOR;

    @Override
    public String echo(String s) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < factor; i++) {
            result.append(s);
        }
        return result.toString();
    }

    @PUT
    public void setFactor(String factor) {
        this.factor = ensureValidInput(factor);
    }

    @GET
    public String getFactor() {
        return Integer.toString(factor);
    }

    private int ensureValidInput(String factor) throws WebApplicationException {
        try {
            final int newValue = Integer.parseInt(factor);
            if (newValue < MIN_FACTOR) {
                throw createWebAppException(String.format("New factor can not be lesser then %d!", MIN_FACTOR));
            }
            return newValue;
        } catch (NumberFormatException nfe) {
            throw createWebAppException(String.format("Error parsing %s as an integer!", factor));
        }
    }

    private WebApplicationException createWebAppException(String message) {

        return new WebApplicationException(

                Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.TEXT_PLAIN)
                        .entity(Entity.text(message)).build());
    }
}
