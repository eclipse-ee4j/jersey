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

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import javax.persistence.EntityManager;

import org.glassfish.jersey.examples.bookmark.entity.BookmarkEntity;
import org.glassfish.jersey.examples.bookmark.entity.BookmarkEntityPK;
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
public class BookmarkResource {

    UriInfo uriInfo; // actual uri info provided by parent resource
    EntityManager em; // entity manager provided by parent resource

    BookmarkEntity bookmarkEntity;

    /**
     * Creates a new instance of UserResource
     */
    public BookmarkResource(UriInfo uriInfo, EntityManager em, UserEntity userEntity, String bmid) {
        this.uriInfo = uriInfo;
        this.em = em;
        bookmarkEntity = em.find(BookmarkEntity.class, new BookmarkEntityPK(bmid, userEntity.getUserid()));
        if (null == bookmarkEntity) {
            throw new ExtendedNotFoundException("bookmark with userid="
                    + userEntity.getUserid() + " and bmid="
                    + bmid + " not found\n");
        }
        bookmarkEntity.setUserEntity(userEntity);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject getBookmark() {
        return asJson();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putBookmark(JSONObject jsonEntity) throws JSONException {

        bookmarkEntity.setLdesc(jsonEntity.getString("ldesc"));
        bookmarkEntity.setSdesc(jsonEntity.getString("sdesc"));
        bookmarkEntity.setUpdated(new Date());

        TransactionManager.manage(new Transactional(em) {
            public void transact() {
                em.merge(bookmarkEntity);
            }
        });
    }

    @DELETE
    public void deleteBookmark() {
        TransactionManager.manage(new Transactional(em) {
            public void transact() {
                UserEntity userEntity = bookmarkEntity.getUserEntity();
                userEntity.getBookmarkEntityCollection().remove(bookmarkEntity);
                em.merge(userEntity);
                em.remove(bookmarkEntity);
            }
        });
    }

    public JSONObject asJson() {
        try {
            return new JSONObject().put("userid", bookmarkEntity.getBookmarkEntityPK().getUserid())
                    .put("sdesc", bookmarkEntity.getSdesc())
                    .put("ldesc", bookmarkEntity.getLdesc())
                    .put("uri", bookmarkEntity.getUri());
        } catch (JSONException je) {
            return null;
        }
    }

    public String toString() {
        return bookmarkEntity.getBookmarkEntityPK().getUserid();
    }
}
