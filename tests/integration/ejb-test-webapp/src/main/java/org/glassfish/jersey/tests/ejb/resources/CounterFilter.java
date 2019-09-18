/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Response filter implemented as EJB bean. The filter adds Request-Count response header to each response.
 * Another EJB singleton bean, CounterBean, is injected that holds the actual request count.
 *
 * @author Jakub Podlesak
 */
@Provider
@Stateless
public class CounterFilter implements ContainerResponseFilter{

    public static final String RequestCountHEADER = "Request-Count";

    @EJB CounterBean counter;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add(RequestCountHEADER, counter.incrementAndGet());
    }
}
