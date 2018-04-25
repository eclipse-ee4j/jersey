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
import static org.glassfish.jersey.examples.exception.Exceptions.MySubSubException;
import static org.glassfish.jersey.examples.exception.Exceptions.WebApplicationExceptionMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ExceptionMappingTest class.
 *
 * @author Santiago.PericasGeertsen at oracle.com
 */
public class ExceptionMappingTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        // mvn test -Djersey.test.containerFactory=org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory
        // mvn test -Djersey.test.containerFactory=org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory
        return new ResourceConfig(
                ExceptionResource.class,
                MyResponseFilter.class,
                MyExceptionMapper.class,
                MySubExceptionMapper.class,
                MySubSubException.class,
                WebApplicationExceptionMapper.class);
    }

    /**
     * Ensure we can access resource with response filter installed.
     */
    @Test
    public void testPingAndFilter() {
        WebTarget t = client().target(UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).build());
        Response r = t.request("text/plain").get();
        assertEquals(200, r.getStatus());
        assertTrue(r.readEntity(String.class).contains(MyResponseFilter.class.getSimpleName()));
    }

    /**
     * No mapper should be used if WebApplicationException already contains a
     * Response with a non-empty entity.
     */
    @Test
    public void testWebApplicationExceptionWithEntity() {
        WebTarget t = client().target(UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).path("webapplication_entity").build());
        Response r = t.request("text/plain").post(Entity.text("Code:200"));
        assertEquals(200, r.getStatus());
        final String entity = r.readEntity(String.class);
        assertTrue(entity.contains("Code:200"));
        assertTrue(entity.contains(MyResponseFilter.class.getSimpleName()));
    }

    /**
     * No mapper should be used if WebApplicationException already contains a
     * Response with a non-empty entity. Same as last test but using 400 code.
     */
    @Test
    public void testWebApplicationExceptionWithEntity400() {
        WebTarget t = client().target(UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).path("webapplication_entity").build());
        Response r = t.request("text/plain").post(Entity.text("Code:400"));
        assertEquals(400, r.getStatus());
        final String entity = r.readEntity(String.class);
        assertTrue(entity.contains("Code:400"));
        assertTrue(entity.contains(MyResponseFilter.class.getSimpleName()));
    }

    /**
     * WebApplicationExceptionMapper should be used if WebApplicationException contains
     * empty entity.
     */
    @Test
    public void testWebApplicationExceptionUsingMapper() {
        WebTarget t = client()
                .target(UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).path("webapplication_noentity").build());
        Response r = t.request("text/plain").post(Entity.text("Code:200"));
        assertEquals(200, r.getStatus());
        String entity = r.readEntity(String.class);
        assertTrue(entity.contains("Code:200"));
        assertTrue(entity.contains(WebApplicationExceptionMapper.class.getSimpleName()));
        assertTrue(entity.contains(MyResponseFilter.class.getSimpleName()));
    }

    /**
     * MyExceptionMapper should be used if MyException is thrown.
     */
    @Test
    public void testMyException() {
        WebTarget t = client().target(UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).path("my").build());
        Response r = t.request("text/plain").post(Entity.text("Code:200"));
        assertEquals(200, r.getStatus());
        String entity = r.readEntity(String.class);
        assertTrue(entity.contains("Code:200"));
        assertTrue(entity.contains(MyExceptionMapper.class.getSimpleName()));
        assertTrue(entity.contains(MyResponseFilter.class.getSimpleName()));
    }

    /**
     * MySubExceptionMapper should be used if MySubException is thrown.
     */
    @Test
    public void testMySubException() {
        WebTarget t = client().target(UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).path("mysub").build());
        Response r = t.request("text/plain").post(Entity.text("Code:200"));
        assertEquals(200, r.getStatus());
        String entity = r.readEntity(String.class);
        assertTrue(entity.contains("Code:200"));
        assertTrue(entity.contains(MySubExceptionMapper.class.getSimpleName()));
        assertTrue(entity.contains(MyResponseFilter.class.getSimpleName()));
    }

    /**
     * MySubExceptionMapper should be used if MySubSubException is thrown, given that
     * there is no mapper for MySubSubException and MySubException is the nearest
     * super type.
     */
    @Test
    public void testMySubSubException() {
        WebTarget t = client().target(UriBuilder.fromUri(getBaseUri()).path(App.ROOT_PATH).path("mysub").build());
        Response r = t.request("text/plain").post(Entity.text("Code:200"));
        assertEquals(200, r.getStatus());
        String entity = r.readEntity(String.class);
        assertTrue(entity.contains("Code:200"));
        assertTrue(entity.contains(MySubExceptionMapper.class.getSimpleName()));
        assertTrue(entity.contains(MyResponseFilter.class.getSimpleName()));
    }
}
