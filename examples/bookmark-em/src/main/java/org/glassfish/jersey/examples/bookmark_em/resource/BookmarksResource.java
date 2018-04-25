/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark_em.resource;

import java.net.URI;
import java.util.Collection;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.glassfish.jersey.examples.bookmark_em.entity.BookmarkEntity;
import org.glassfish.jersey.examples.bookmark_em.util.tx.TransactionManager;
import org.glassfish.jersey.examples.bookmark_em.util.tx.Transactional;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
public class BookmarksResource {

    UriInfo uriInfo; // actual uri info
    EntityManager em; // entity manager provided by parent resource
    UserTransaction utx; // user transaction provided by parent resource

    UserResource userResource; // parent user resource

    /**
     * Creates a new instance of BookmarksResource
     */
    public BookmarksResource(UriInfo uriInfo, EntityManager em, UserTransaction utx, UserResource userResource) {
        this.uriInfo = uriInfo;
        this.em = em;
        this.utx = utx;
        this.userResource = userResource;
    }

    public Collection<BookmarkEntity> getBookmarks() {
        return userResource.getUserEntity().getBookmarkEntityCollection();
    }

    @Path("{bmid: .+}")
    public BookmarkResource getBookmark(@PathParam("bmid") String bmid) {
        return new BookmarkResource(uriInfo, em, utx, userResource.getUserEntity(), bmid);
    }

    @GET
    @Produces("application/json")
    public JSONArray getBookmarksAsJsonArray() {
        JSONArray uriArray = new JSONArray();
        for (BookmarkEntity bookmarkEntity : getBookmarks()) {
            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI bookmarkUri = ub
                    .path(bookmarkEntity.getBookmarkEntityPK().getBmid())
                    .build();
            uriArray.put(bookmarkUri.toASCIIString());
        }
        return uriArray;
    }

    @POST
    @Consumes("application/json")
    public Response postForm(JSONObject bookmark) throws JSONException {
        final BookmarkEntity bookmarkEntity = new BookmarkEntity(getBookmarkId(bookmark.getString("uri")),
                userResource.getUserEntity().getUserid());

        bookmarkEntity.setUri(bookmark.getString("uri"));
        bookmarkEntity.setUpdated(new Date());
        bookmarkEntity.setSdesc(bookmark.getString("sdesc"));
        bookmarkEntity.setLdesc(bookmark.getString("ldesc"));
        userResource.getUserEntity().getBookmarkEntityCollection().add(bookmarkEntity);

        TransactionManager.manage(utx, new Transactional(em) {
            public void transact() {
                em.merge(userResource.getUserEntity());
            }
        });

        URI bookmarkUri = uriInfo.getAbsolutePathBuilder()
                .path(bookmarkEntity.getBookmarkEntityPK().getBmid())
                .build();
        return Response.created(bookmarkUri).build();
    }

    private String getBookmarkId(String uri) {
        return uri;
    }
}
