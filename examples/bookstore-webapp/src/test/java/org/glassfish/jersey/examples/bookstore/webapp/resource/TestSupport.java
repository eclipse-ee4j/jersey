/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookstore.webapp.resource;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.examples.bookstore.webapp.MyApplication;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.JerseyTest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author James Strachan
 * @author Naresh
 * @author Michal Gajdos
 */
public abstract class TestSupport extends JerseyTest {

    @Override
    protected Application configure() {
        final MyApplication application = new MyApplication();
        application.register(JspMvcFeature.class);
        application.property(ServletProperties.FILTER_FORWARD_ON_404, true);
        return application;
    }

    protected void assertHtmlResponse(String response) {
        assertNotNull("No text returned!", response);

        assertResponseContains(response, "<html>");
        assertResponseContains(response, "</html>");
    }

    protected void assertResponseContains(String response, String text) {
        assertTrue("Response should contain " + text + " but was: " + response, response.contains(text));
    }
}
