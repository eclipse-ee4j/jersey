/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonp;

import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JsonWithPaddingTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return App.createApp();
    }

    /**
     * Test checks that the application.wadl is reachable.
     * <p/>
     */
    @Test
    public void testApplicationWadl() {
        WebTarget target = target();
        String applicationWadl = target.path("application.wadl").request().get(String.class);
        assertTrue("Something wrong. Returned wadl length is not > 0", applicationWadl.length() > 0);
    }

    /**
     * Test check GET on the "changes" resource in "application/json" format.
     */
    @Test
    public void testGetOnChangesJSONFormat() {
        WebTarget target = target();
        GenericType<List<ChangeRecordBean>> genericType = new GenericType<List<ChangeRecordBean>>() {};
        // get the initial representation
        List<ChangeRecordBean> changes = target.path("changes").request("application/json").get(genericType);
        // check that there are two changes entries
        assertEquals("Expected number of initial changes not found", 5, changes.size());
    }

    /**
     * Test check GET on the "changes" resource in "application/xml" format.
     */
    @Test
    public void testGetOnLatestChangeXMLFormat() {
        WebTarget target = target();
        ChangeRecordBean lastChange = target.path("changes/latest").request("application/xml").get(ChangeRecordBean.class);
        assertEquals(1, lastChange.linesChanged);
    }

    /**
     * Test check GET on the "changes" resource in "application/javascript" format.
     */
    @Test
    public void testGetOnLatestChangeJavascriptFormat() {
        WebTarget target = target();
        String js = target.path("changes").request("application/x-javascript").get(String.class);
        assertTrue(js.startsWith("callback"));
    }

    @Test
    public void testGetOnLatestChangeJavascriptFormatDifferentCallback() {
        WebTarget target = target();
        String js = target.path("changes").queryParam("__callback", "parse").request("application/x-javascript")
                .get(String.class);
        assertTrue(js.startsWith("parse"));
    }
}
