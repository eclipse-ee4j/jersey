/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark;

import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Michal Gajdos
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookmarkTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new MyApplication();
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("bookmark").build();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JettisonFeature.class);
    }

    @Test
    public void step1_getUsers() {
        final JSONArray users = target().path("resources/users/").request("application/json").get(JSONArray.class);
        assertTrue(users != null);
    }

    @Test
    public void step2_createUser() {
        boolean thrown = false;
        JSONObject user = new JSONObject();

        try {
            user.put("userid", "testuid").put("password", "test").put("email", "test@test.net").put("username", "Test User");
            target().path("resources/users/testuid").request().put(Entity.entity(user, "application/json"));
        } catch (Exception e) {
            e.printStackTrace();
            thrown = true;
        }

        assertFalse(thrown);
    }

    @Test
    public void step3_getUsers2() {
        final JSONArray users = target().path("resources/users/").request("application/json").get(JSONArray.class);
        assertTrue(users != null);
        assertTrue(users.length() == 1);
    }

    @Test
    public void step4_updateUser() {
        boolean thrown = false;

        try {
            JSONObject user = target().path("resources/users/testuid").request("application/json").get(JSONObject.class);

            user.put("password", "NEW PASSWORD").put("email", "NEW@EMAIL.NET").put("username", "UPDATED TEST USER");
            target().path("resources/users/testuid").request().put(Entity.entity(user, "application/json"));

            user = target().path("resources/users/testuid").request("application/json").get(JSONObject.class);

            assertEquals(user.get("username"), "UPDATED TEST USER");
            assertEquals(user.get("email"), "NEW@EMAIL.NET");
            assertEquals(user.get("password"), "NEW PASSWORD");

        } catch (Exception e) {
            e.printStackTrace();
            thrown = true;
        }

        assertFalse(thrown);
    }

    // ugly.. but separation into separate test cases would be probably uglier
    @Test
    public void step5_getUserBookmarkList() {
        boolean thrown = false;

        try {
            JSONObject user = target().path("resources/users/testuid").request("application/json").get(JSONObject.class);
            assertTrue(user != null);

            final WebTarget webTarget = client().target(user.getString("bookmarks"));

            JSONObject bookmark = new JSONObject();
            bookmark.put("uri", "http://java.sun.com").put("sdesc", "test desc").put("ldesc", "long test description");
            webTarget.request().post(Entity.entity(bookmark, "application/json"));

            JSONArray bookmarks = webTarget.request("application/json").get(JSONArray.class);
            assertTrue(bookmarks != null);
            int bookmarksSize = bookmarks.length();

            String testBookmarkUrl = bookmarks.getString(0);
            final WebTarget bookmarkResource = client().target(testBookmarkUrl);

            bookmark = bookmarkResource.request("application/json").get(JSONObject.class);
            assertTrue(bookmark != null);

            bookmarkResource.request().delete();

            bookmarks = target().path("resources/users/testuid/bookmarks").request("application/json").get(JSONArray.class);
            assertTrue(bookmarks != null);
            assertTrue(bookmarks.length() == (bookmarksSize - 1));
        } catch (Exception e) {
            e.printStackTrace();
            thrown = true;
        }

        assertFalse(thrown);
    }

    @Test
    public void step6_deleteUser() {
        boolean thrown = false;

        try {
            target().path("resources/users/testuid").request().delete();
        } catch (Exception e) {
            e.printStackTrace();
            thrown = true;
        }

        assertFalse(thrown);
    }
}
