/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.freemarker;

import java.net.URI;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class FreemarkerTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new MyApplication().property(FreemarkerMvcFeature.TEMPLATE_BASE_PATH, "freemarker");
    }

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("freemarker-webapp").build();
    }

    @Test
    public void testHello() {
        assertTrue(target().path("hello").request().get(String.class).contains("Pavel"));
    }

    @Test
    public void testHellowithDefaultModel() {
        assertTrue(target().path("hello-default-model").request().get(String.class).contains("Pavel"));
    }

    @Test
    public void testAutoTemplate() {
        assertTrue(target().path("autoTemplate").request().get(String.class).contains("Pavel"));
    }

    @Test
    public void testAutoTemplateWithoutSuffix() {
        assertTrue(target().path("helloWithoutSuffix").request().get(String.class).contains("Pavel"));
    }

}
