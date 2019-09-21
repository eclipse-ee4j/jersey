/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedclientsimple;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import org.glassfish.jersey.examples.managedclientsimple.resources.ManagedClientApplication;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Miroslav Fuksa
 *
 */
public class ManagedClientSimpleTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ManagedClientApplication();
    }

    @Override
    protected URI getBaseUri() {
        final UriBuilder baseUriBuilder = UriBuilder.fromUri(super.getBaseUri()).path("managed-client-simple-webapp");
        final boolean externalFactoryInUse = getTestContainerFactory() instanceof ExternalTestContainerFactory;
        return externalFactoryInUse ? baseUriBuilder.path("app").build() : baseUriBuilder.build();
    }

    @Test
    public void testManagedClientSimple() throws Exception {
        final WebTarget resource = target().path("client");
        Response response;

        response = resource.path("animals").request(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatus());
        assertEquals("Queried animals: Max and Lucy and Bobo", response.readEntity(String.class));

        response = resource.path("car/1").request(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatus());
        assertEquals("Response from resource/car/1: CAR with id=1", response.readEntity(String.class));
    }

}

