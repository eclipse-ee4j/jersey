/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.util.logging.Logger;

import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * JAX-RS exception mapper registered as CDI managed bean.
 *
 * @author Paul Sandoz
 */
@Provider
@ApplicationScoped
public class JCDIBeanExceptionMapper implements ExceptionMapper<JDCIBeanException> {

    private static final Logger LOGGER = Logger.getLogger(JCDIBeanExceptionMapper.class.getName());

    @Context
    private UriInfo uiFieldInject;

    @Context
    private ResourceContext rc;

    private UriInfo uiMethodInject;

    @Context
    public void set(UriInfo ui) {
        this.uiMethodInject = ui;
    }

    @PostConstruct
    public void postConstruct() {
        ensureInjected();
        LOGGER.info(String.format("In post construct of %s", this));
    }

    @Override
    public Response toResponse(JDCIBeanException exception) {
        ensureInjected();
        return Response.serverError().entity("JDCIBeanException").build();
    }

    private void ensureInjected() throws IllegalStateException {
        if (uiFieldInject == null || uiMethodInject == null || rc == null) {
            throw new IllegalStateException();
        }
    }
}
