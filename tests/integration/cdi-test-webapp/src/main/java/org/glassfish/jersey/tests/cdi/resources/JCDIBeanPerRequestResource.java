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

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * Request scoped JAX-RS resource registered.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("/jcdibean/per-request")
@RequestScoped
public class JCDIBeanPerRequestResource {

    private static final Logger LOGGER = Logger.getLogger(JCDIBeanPerRequestResource.class.getName());

    @Resource(name = "injectedResource")
    private int injectedResource = 0;

    @Context
    private UriInfo uiFieldInject;

    @Context
    private ResourceContext rc;

    @QueryParam("x")
    private String x;

    private UriInfo uiMethodInject;

    @Context
    public void set(UriInfo ui) {
        this.uiMethodInject = ui;
    }

    @PostConstruct
    public void postConstruct() {

        ensureInjected();

        LOGGER.info(String.format(
                "In post construct of %s; uiFieldInject: %s; uiMethodInject: %s",
                this, uiFieldInject, uiMethodInject));
    }

    @GET
    @Produces("text/plain")
    public String getMessage() {

        ensureInjected();

        LOGGER.info(String.format(
                "In getMessage of %s; uiFieldInject: %s; uiMethodInject: %s",
                this, uiFieldInject, uiMethodInject));

        return String.format("%s: queryParam=%s %d", uiFieldInject.getRequestUri().toString(), x, injectedResource++);
    }

    @PreDestroy
    public void preDestroy() {
        LOGGER.info(String.format("In pre destroy of %s", this));
    }

    private void ensureInjected() throws IllegalStateException {
        if (uiFieldInject == null || uiMethodInject == null || rc == null) {
            throw new IllegalStateException();
        }
    }
}
