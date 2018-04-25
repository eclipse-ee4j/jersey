/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;

/**
 * JAX-RS exception mapper registered as a CDI managed bean.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Provider
@ManagedBean
public class JCDIBeanDependentExceptionMapper implements ExceptionMapper<JDCIBeanDependentException> {

    private static final Logger LOGGER = Logger.getLogger(JCDIBeanDependentExceptionMapper.class.getName());

    @Context
    private UriInfo uiFieldInject;

    @Context
    private ResourceContext resourceContext;

    private UriInfo uiMethodInject;

    @Context
    public void set(UriInfo ui) {
        this.uiMethodInject = ui;
    }

    @PostConstruct
    public void postConstruct() {
        LOGGER.log(Level.INFO, String.format("In post construct of %s", this));
        ensureInjected();
    }

    @Override
    public Response toResponse(JDCIBeanDependentException exception) {
        ensureInjected();
        return Response.serverError().entity("JDCIBeanDependentException").build();
    }

    private void ensureInjected() throws IllegalStateException {
        if (uiFieldInject == null || uiMethodInject == null || resourceContext == null) {
            throw new IllegalStateException();
        }
    }
}
