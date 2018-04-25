/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedbeans.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/**
 * JAX-RS root resource treated as Java EE managed bean in singleton scope.
 *
 * @author Paul Sandoz
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("/managedbean/singleton")
@Singleton
@ManagedBean
public class ManagedBeanSingletonResource {

    /**
     * Initial value should get injected by Java EE container.
     */
    @Resource(name = "injectedResource")
    int counter = 0;

    @Context
    UriInfo ui;

    private EntityManager entityManager;

    /**
     * Set entity manager based on injected entity manager factory.
     *
     * @param em entity manager factory injected by Java EE container.
     */
    @PersistenceUnit(unitName = "ManagedBeansPU")
    void setEntityManager(final EntityManagerFactory em) {
        entityManager = em.createEntityManager();
    }

    /**
     * Provide textual representation of the internal counter.
     * @return internal counter {@code String} representation
     */
    @GET
    @Produces("text/plain")
    public String getMessage() {
        return Integer.toString(counter++);
    }

    /**
     * Reset internal counter.
     * @param i new counter value to be set.
     */
    @PUT
    @Produces("text/plain")
    public void putMessage(final int i) {
        counter = i;
    }

    /**
     * Throw a runtime exception, so that it could be mapped.
     *
     * @return nothing, just throw the custom exception.
     */
    @Path("exception")
    public String getException() {
        throw new ManagedBeanException();
    }

    /**
     * Get JPA widget value.
     *
     * @param id widget id.
     * @return value of the widget or 404 if given widget id is not found.
     */
    @Path("widget/{id: \\d+}")
    @GET
    public String getWidget(@PathParam("id") final int id) {
        try {
            return entityManager.find(Widget.class, id).val;
        } catch (final NullPointerException ignored) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    /**
     * Set new widget value.
     * @param id widget id.
     * @param val new value to be associated with above specified id.
     */
    @Path("widget/{id: \\d+}")
    @PUT
    public void putWidget(@PathParam("id") final int id, final String val) {
        entityManager.merge(new Widget(id, val));
    }

    /**
     * Remove widget with given id.
     * @param id id of the widget to be removed.
     */
    @Path("widget/{id: \\d+}")
    @DELETE
    public void deleteWidget(@PathParam("id") final int id) {
        entityManager.remove(entityManager.find(Widget.class, id));
    }
}
