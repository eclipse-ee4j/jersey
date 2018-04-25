/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark_em.resource;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriInfo;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.glassfish.jersey.examples.bookmark_em.entity.BookmarkEntity;
import org.glassfish.jersey.examples.bookmark_em.entity.BookmarkEntityPK;
import org.glassfish.jersey.examples.bookmark_em.entity.UserEntity;
import org.glassfish.jersey.examples.bookmark_em.exception.ExtendedNotFoundException;
import org.glassfish.jersey.examples.bookmark_em.util.tx.TransactionManager;
import org.glassfish.jersey.examples.bookmark_em.util.tx.Transactional;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class BookmarkResource {

    UriInfo uriInfo; // actual uri info provided by parent resource
    EntityManager em; // entity manager provided by parent resource
    UserTransaction utx; //user transaction provided by parent resource

    BookmarkEntity bookmarkEntity;

    /**
     * Creates a new instance of UserResource
     */
    public BookmarkResource(UriInfo uriInfo, EntityManager em, UserTransaction utx, UserEntity userEntity, String bmid) {
        this.uriInfo = uriInfo;
        this.em = em;
        this.utx = utx;

        bookmarkEntity = em.find(BookmarkEntity.class, new BookmarkEntityPK(bmid, userEntity.getUserid()));
        if (null == bookmarkEntity) {
            throw new ExtendedNotFoundException("bookmark with userid="
                    + userEntity.getUserid() + " and bmid="
                    + bmid + " not found\n");
        }
        bookmarkEntity.setUserEntity(userEntity);
    }

    @GET
    @Produces("application/json")
    public JSONObject getBookmark() {
        return asJson();
    }

    @PUT
    @Consumes("application/json")
    public void putBookmark(JSONObject jsonEntity) throws JSONException {

        bookmarkEntity.setLdesc(jsonEntity.getString("ldesc"));
        bookmarkEntity.setSdesc(jsonEntity.getString("sdesc"));
        bookmarkEntity.setUpdated(new Date());

        TransactionManager.manage(utx, new Transactional(em) {
            public void transact() {
                em.merge(bookmarkEntity);
            }
        });
    }

    @DELETE
    public void deleteBookmark() {
        TransactionManager.manage(utx, new Transactional(em) {
            public void transact() {
                em.persist(bookmarkEntity);
                UserEntity userEntity = bookmarkEntity.getUserEntity();
                userEntity.getBookmarkEntityCollection().remove(bookmarkEntity);
                em.merge(userEntity);
                em.remove(bookmarkEntity);
            }
        });
    }

    public String asString() {
        return toString();
    }

    public JSONObject asJson() {
        try {
            return new JSONObject()
                    .put("userid", bookmarkEntity.getBookmarkEntityPK().getUserid())
                    .put("sdesc", bookmarkEntity.getSdesc())
                    .put("ldesc", bookmarkEntity.getLdesc())
                    .put("uri", bookmarkEntity.getUri());
        } catch (JSONException je) {
            return null;
        }
    }

    @Override
    public String toString() {
        return bookmarkEntity.getBookmarkEntityPK().getUserid();
    }
}
