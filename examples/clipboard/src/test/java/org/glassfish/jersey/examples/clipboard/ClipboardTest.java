/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.clipboard;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ClipboardTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return App.createApp();
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        client().target(getBaseUri()).path(App.ROOT_PATH).request().delete();
        client().target(getBaseUri()).path(App.ROOT_PATH).path("history").request().delete();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(ClipboardDataProvider.ApplicationJson.class).register(ClipboardDataProvider.TextPlain.class);
    }

    @Test
    public void testDeclarativeClipboardTextPlain() throws Exception {
        testDeclarativeClipboard(MediaType.TEXT_PLAIN_TYPE);
    }

    @Test
    public void testDeclarativeClipboardAppJson() throws Exception {
        testDeclarativeClipboard(MediaType.APPLICATION_JSON_TYPE);
    }

    public void testDeclarativeClipboard(MediaType mediaType) throws Exception {
        final WebTarget clipboard = client().target(getBaseUri()).path(App.ROOT_PATH);

        Response response;

        response = clipboard.request(mediaType).get();
        assertEquals(204, response.getStatus());

        response = clipboard.request(mediaType).put(Entity.entity(new ClipboardData("Hello"), mediaType));
        assertEquals(204, response.getStatus());

        assertEquals("Hello", clipboard.request(mediaType).get(ClipboardData.class).toString());

        response = clipboard.request(mediaType).post(Entity.entity(new ClipboardData(" World!"), mediaType));
        assertEquals(200, response.getStatus());

        assertEquals("Hello World!", clipboard.request(mediaType).get(ClipboardData.class).toString());

        response = clipboard.request(mediaType).delete();
        assertEquals(204, response.getStatus());

        assertEquals(204, clipboard.request(mediaType).get().getStatus());
    }

    @Test
    public void testProgrammaticEchoTextPlain() throws Exception {
        testProgrammaticEcho(MediaType.TEXT_PLAIN_TYPE);
    }

    @Test
    public void testProgrammaticEchoAppJson() throws Exception {
        testProgrammaticEcho(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void testHistoryInJson() throws Exception {
        callGetHistory(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void testHistoryInTextPlain() throws Exception {
        callGetHistory(MediaType.TEXT_PLAIN_TYPE);
    }

    private void callGetHistory(final MediaType mediaType) {
        final WebTarget clipboard = client().target(getBaseUri()).path(App.ROOT_PATH);
        clipboard.request(mediaType).post(Entity.entity(new ClipboardData("Task 1 "), mediaType));
        clipboard.request(mediaType).post(Entity.entity(new ClipboardData("Task 2 "), mediaType));
        clipboard.request(mediaType).post(Entity.entity(new ClipboardData("Task 3 "), mediaType));

        ClipboardData response = clipboard.path("history").request(mediaType).get(ClipboardData.class);
        assertEquals(new ClipboardData("[Task 1 , Task 1 Task 2 ]"), response);
    }

    public void testProgrammaticEcho(MediaType mediaType) throws Exception {
        final WebTarget echo = client().target(getBaseUri()).path("echo");

        Response response = echo.request(mediaType).post(Entity.entity(new ClipboardData("Hello"), mediaType));
        assertEquals("Hello", response.readEntity(ClipboardData.class).toString());
    }
}
