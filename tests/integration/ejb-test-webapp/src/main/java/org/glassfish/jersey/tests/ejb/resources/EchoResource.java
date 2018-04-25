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

package org.glassfish.jersey.tests.ejb.resources;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * JAX-RS resource bean backed by an EJB session bean
 * implementing EJB interface that is annotated with both {@link Local}
 * and {@link Remote} annotations.
 * Reproducible test case for GLASSFISH-16199.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Stateless
@Path("echo")
public class EchoResource implements Echo {

    @EJB EchoBean echoService;

    @GET
    @Override
    public String echo(@QueryParam("message") String message) {
        return echoService.echo(message);
    }
}
