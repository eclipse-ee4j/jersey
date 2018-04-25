/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import javax.jws.WebResult;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.tests.integration.j59.ejb.lib.LocalBeanWithRemoteInterface;

/**
 * Part of CDI extension lookup issue reproducer.
 * This bean will CDI-inject a local EJB bean.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
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
