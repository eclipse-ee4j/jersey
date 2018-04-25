/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.linking;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.examples.linking.resources.ItemsResource;
import org.glassfish.jersey.linking.DeclarativeLinkingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
public class LinkWebAppTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        final ResourceConfig rc = new ResourceConfig(ItemsResource.class);
        rc.register(DeclarativeLinkingFeature.class);
        return rc;
    }

    /**
     * Test that the expected response is sent back.
     */
    @Test
    public void testLinks() throws Exception {
        final Response response = target().path("items")
                .queryParam("offset", 10)
                .queryParam("limit", "10")
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(Response.class);

        final Response.StatusType statusInfo = response.getStatusInfo();
        assertEquals("Should have succeeded", 200, statusInfo.getStatusCode());

        final String content = response.readEntity(String.class);
        final List<Object> linkHeaders = response.getHeaders().get("Link");

        assertEquals("Should have two link headers", 2, linkHeaders.size());
        assertThat("Content should contain next link",
                content,
                containsString("http://localhost:" + getPort() + "/items?offset=20&amp;limit=10"));
    }
}
