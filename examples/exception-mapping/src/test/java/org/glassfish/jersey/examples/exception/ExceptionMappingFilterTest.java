/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.exception;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.glassfish.jersey.examples.exception.ExceptionResource.MyResponseFilter;
import static org.glassfish.jersey.examples.exception.Exceptions.MyExceptionMapper;
import static org.glassfish.jersey.examples.exception.Exceptions.MySubExceptionMapper;
import static org.glassfish.jersey.examples.exception.Exceptions.WebApplicationExceptionMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ExceptionMappingFilterTest class.
 *
 * @author Santiago.PericasGeertsen at oracle.com
 */
public class ExceptionMappingFilterTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        // mvn test -Djersey.test.containerFactory=org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory
        // mvn test -Djersey.test.containerFactory=org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory

        final ResourceConfig resourceConfig = new ResourceConfig(
                ExceptionResource.class,
                MyResponseFilter.class,
                ExceptionResource.WebApplicationExceptionFilter.class,
                MyExceptionMapper.class,
                MySubExceptionMapper.class,
                WebApplicationExceptionMapper.class);

        return resourceConfig;
    }

    /**
     * Instructs request filter to throw a WebApplicationException which must be mapped
     * to a response and processed through response pipeline.
     */
    @Test
    public void testWebApplicationExceptionInRequestFilter() {
        WebTarget t = client().target(UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).path("request_exception").build());
        Response r = t.request("text/plain").post(Entity.text("Request Exception"));
        assertEquals(200, r.getStatus());
        final String entity = r.readEntity(String.class);
        System.out.println("entity = " + entity);
        assertTrue(entity.contains(MyResponseFilter.class.getSimpleName()));
    }

    @Test
    public void testWebApplicationExceptionInResponseFilter() {
        WebTarget t = client().target(UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).path("response_exception").build());
        Response r = t.request("text/plain").get();
        assertEquals(200, r.getStatus());
        final String entity = r.readEntity(String.class);
        System.out.println("entity = " + entity);
        assertTrue(entity.contains(MyResponseFilter.class.getSimpleName()));
    }
}
