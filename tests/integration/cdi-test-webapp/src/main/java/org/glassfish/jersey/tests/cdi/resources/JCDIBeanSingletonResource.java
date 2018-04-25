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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Application scoped JAX-RS resource.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("/jcdibean/singleton/{p}")
@ApplicationScoped
public class JCDIBeanSingletonResource {

    private static final Logger LOGGER = Logger.getLogger(JCDIBeanSingletonResource.class.getName());

    @Resource(name = "injectedResource")
    private int counter = 0;

    @Context
    private UriInfo uiFieldinject;

    @Context
    private
    ResourceContext resourceContext;

    private UriInfo uiMethodInject;

    @Inject
    Provider<JCDIBeanExceptionMapper> mapperProvider;

    @Context
    public void set(UriInfo ui) {
        this.uiMethodInject = ui;
    }

    @PostConstruct
    public void postConstruct() {
        LOGGER.info(String.format("In post construct of %s", this));
        ensureInjected();
    }

    @GET
    @Produces("text/plain")
    public String getMessage(@PathParam("p") String p) {
        LOGGER.info(String.format(
                "In getMessage in %s; uiFieldInject: %s; uiMethodInject: %s; provider: %s; provider.get(): %s", this,
                uiFieldinject, uiMethodInject, mapperProvider, mapperProvider.get()));
        ensureInjected();

        return String.format("%s: p=%s, queryParam=%s",
                uiFieldinject.getRequestUri().toString(), p, uiMethodInject.getQueryParameters().getFirst("x"));
    }

    @Path("exception")
    public String getException() {
        throw new JDCIBeanException();
    }

    @Path("counter")
    @GET
    public synchronized String getCounter() {
        return Integer.toString(counter++);
    }

    @Path("counter")
    @PUT
    public synchronized void setCounter(String counter) {
        this.counter = Integer.decode(counter);
    }

    @PreDestroy
    public void preDestroy() {
        LOGGER.info(String.format("In pre destroy of %s", this));
    }

    private void ensureInjected() throws IllegalStateException {
        if (uiFieldinject == null || uiMethodInject == null
                || resourceContext == null || mapperProvider.get() == null) {
            throw new IllegalStateException();
        }
    }
}
