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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ClipboardTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return App.createProgrammaticClipboardApp();
    }

    @Test
    public void testClipboard() throws Exception {
        final Client client = client();
        final WebTarget clipboard = client.target(getBaseUri()).path(App.ROOT_PATH);

        Response response;

        response = clipboard.request("text/plain").get();
        assertEquals(204, response.getStatus());

        response = clipboard.request("text/plain").put(Entity.text("Hello"));
        assertEquals(204, response.getStatus());
        assertEquals("Hello", clipboard.request("text/plain").get(String.class));

        response = clipboard.request("text/plain").post(Entity.text(" World!"));
        assertEquals(200, response.getStatus());
        assertEquals("Hello World!", clipboard.request("text/plain").get(String.class));

        response = clipboard.request("text/plain").delete();
        assertEquals(204, response.getStatus());
        assertEquals(204, clipboard.request("text/plain").get().getStatus());
    }
}
