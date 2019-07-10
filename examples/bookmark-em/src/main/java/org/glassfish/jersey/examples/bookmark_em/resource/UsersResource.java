/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark_em.resource;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.glassfish.jersey.examples.bookmark_em.entity.UserEntity;

import org.codehaus.jettison.json.JSONArray;

/**
 * @author Jakub Podlesak
 */
@Path("/users/")
@ManagedBean
public class UsersResource {

    // injected EntityManager property
    @PersistenceContext(unitName = "BookmarkPU")
    private EntityManager em;

    // injected UserTransaction
    @Resource
    private UserTransaction utx;

    @Context
    UriInfo uriInfo;

    /**
     * Creates a new instance of Users
     */
    public UsersResource() {
    }

    @SuppressWarnings("unchecked")
    public List<UserEntity> getUsers() {
        return em.createQuery("SELECT u from UserEntity u").getResultList();
    }

    @Path("{userid}/")
    public UserResource getUser(@PathParam("userid") String userid) {
        return new UserResource(uriInfo, em, utx, userid);
    }

    @GET
    @Produces("application/json")
    public JSONArray getUsersAsJsonArray() {
        JSONArray uriArray = new JSONArray();
        for (UserEntity userEntity : getUsers()) {
            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI userUri = ub
                    .path(userEntity.getUserid())
                    .build();
            uriArray.put(userUri.toASCIIString());
        }
        return uriArray;
    }
}
