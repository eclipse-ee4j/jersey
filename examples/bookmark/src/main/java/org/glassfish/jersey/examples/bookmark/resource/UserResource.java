/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import javax.persistence.EntityManager;

import org.glassfish.jersey.examples.bookmark.entity.UserEntity;
import org.glassfish.jersey.examples.bookmark.exception.ExtendedNotFoundException;
import org.glassfish.jersey.examples.bookmark.util.tx.TransactionManager;
import org.glassfish.jersey.examples.bookmark.util.tx.Transactional;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class UserResource {

    String userid; // userid from url
    UserEntity userEntity; // appropriate jpa user entity

    UriInfo uriInfo; // actual uri info provided by parent resource
    EntityManager em; // entity manager provided by parent resource

    /**
     * Creates a new instance of UserResource
     */
    public UserResource(UriInfo uriInfo, EntityManager em, String userid) {
        this.uriInfo = uriInfo;
        this.userid = userid;
        this.em = em;
        userEntity = em.find(UserEntity.class, userid);
    }

    @Path("bookmarks/")
    public BookmarksResource getBookmarksResource() {
        if (null == userEntity) {
            throw new ExtendedNotFoundException("userid " + userid + " does not exist!");
        }
        return new BookmarksResource(uriInfo, em, this);
    }

    @GET
    @Produces("application/json")
    public JSONObject getUser() throws JSONException {
        if (null == userEntity) {
            throw new ExtendedNotFoundException("userid " + userid + "does not exist!");
        }
        return new JSONObject().put("userid", userEntity.getUserid())
                .put("username", userEntity.getUsername())
                .put("email", userEntity.getEmail())
                .put("password", userEntity.getPassword())
                .put("bookmarks", uriInfo.getAbsolutePathBuilder().path("bookmarks").build());
    }

    @PUT
    @Consumes("application/json")
    public Response putUser(JSONObject jsonEntity) throws JSONException {

        String jsonUserid = jsonEntity.getString("userid");

        if ((null != jsonUserid) && !jsonUserid.equals(userid)) {
            return Response.status(409).entity("userids differ!\n").build();
        }

        final boolean newRecord = (null == userEntity); // insert or update ?

        if (newRecord) { // new user record to be inserted
            userEntity = new UserEntity();
            userEntity.setUserid(userid);
        }
        userEntity.setUsername(jsonEntity.getString("username"));
        userEntity.setEmail(jsonEntity.getString("email"));
        userEntity.setPassword(jsonEntity.getString("password"));

        if (newRecord) {
            TransactionManager.manage(new Transactional(em) {
                public void transact() {
                    em.persist(userEntity);
                }
            });
            return Response.created(uriInfo.getAbsolutePath()).build();
        } else {
            TransactionManager.manage(new Transactional(em) {
                public void transact() {
                    em.merge(userEntity);
                }
            });
            return Response.noContent().build();
        }
    }

    @DELETE
    public void deleteUser() {
        if (null == userEntity) {
            throw new ExtendedNotFoundException("userid " + userid + "does not exist!");
        }
        TransactionManager.manage(new Transactional(em) {
            public void transact() {
                em.remove(userEntity);
            }
        });
    }

    public String toString() {
        return userEntity.getUserid();
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }
}
