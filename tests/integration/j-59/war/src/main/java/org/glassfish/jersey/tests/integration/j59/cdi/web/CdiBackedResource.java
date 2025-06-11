/*
 * Copyright (c) 2014, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.j59.cdi.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import jakarta.jws.WebResult;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.glassfish.jersey.tests.integration.j59.ejb.lib.LocalBeanWithRemoteInterface;

/**
 * Part of CDI extension lookup issue reproducer.
 * This bean will CDI-inject a local EJB bean.
 *
 * @author Jakub Podlesak
 */
@Path("name")
@RequestScoped
public class CdiBackedResource implements ResourceMarkerInterface {

    @Inject
    private LocalBeanWithRemoteInterface localBean;

    @GET
    @Path("hello")
    @Produces(MediaType.TEXT_PLAIN)
    @WebResult(name = "hello")
    public String sayHello() {
        return "Hello " + localBean.getName();
    }
}
