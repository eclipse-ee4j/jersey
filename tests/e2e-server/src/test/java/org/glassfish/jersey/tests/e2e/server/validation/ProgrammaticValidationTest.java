/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.tests.e2e.server.validation;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Bean Validation tests for programmatically created resources.
 *
 * @author Michal Gajdos
 */
public class ProgrammaticValidationTest extends JerseyTest {

    @Override
    protected Application configure() {
        final Set<Resource> resources = new HashSet<>();

        Resource.Builder resourceBuilder = Resource.builder("instance");
        resourceBuilder
                .addMethod("POST")
                .handledBy(new ValidationInflector());
        resources.add(resourceBuilder.build());

        resourceBuilder = Resource.builder("class");
        resourceBuilder
                .addMethod("POST")
                .handledBy(ValidationInflector.class);
        resources.add(resourceBuilder.build());

        try {
            resourceBuilder = Resource.builder("methodInstanceClass");
            resourceBuilder
                    .addMethod("POST")
                    .handledBy(new ValidationInflector(), ValidationInflector.class.getMethod("get",
                            ContainerRequestContext.class));
            resources.add(resourceBuilder.build());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException();
        }

        try {
            resourceBuilder = Resource.builder("methodClassClass");
            resourceBuilder
                    .addMethod("POST")
                    .handledBy(ValidationInflector.class,
                            ValidationInflector.class.getMethod("get", ContainerRequestContext.class));
            resources.add(resourceBuilder.build());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException();
        }

        try {
            resourceBuilder = Resource.builder("methodInstanceInterface");
            resourceBuilder
                    .addMethod("POST")
                    .handledBy(new ValidationInflector(), Inflector.class.getMethod("apply", Object.class));
            resources.add(resourceBuilder.build());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException();
        }

        try {
            resourceBuilder = Resource.builder("methodClassInterface");
            resourceBuilder
                    .addMethod("POST")
                    .handledBy(ValidationInflector.class, Inflector.class.getMethod("apply", Object.class));
            resources.add(resourceBuilder.build());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException();
        }

        return new ResourceConfig().register(LoggingFeature.class).registerResources(resources);
    }

    @Test
    public void testInflectorInstance() throws Exception {
        final Response response = target("instance").request().post(Entity.entity("value", MediaType.TEXT_PLAIN_TYPE));

        assertEquals(200, response.getStatus());
        assertEquals("value", response.readEntity(String.class));
    }

    @Test
    public void testInflectorInstanceNegative() throws Exception {
        final Response response = target("instance").request().post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));

        assertEquals(500, response.getStatus());
    }

    @Test
    public void testInflectorClass() throws Exception {
        final Response response = target("class").request().post(Entity.entity("value", MediaType.TEXT_PLAIN_TYPE));

        assertEquals(200, response.getStatus());
        assertEquals("value", response.readEntity(String.class));
    }

    @Test
    public void testInflectorClassNegative() throws Exception {
        final Response response = target("class").request().post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));

        assertEquals(500, response.getStatus());
    }

    @Test
    public void testInflectorMethodInstanceClass() throws Exception {
        final Response response = target("methodInstanceClass").request().post(Entity.entity("value", MediaType.TEXT_PLAIN_TYPE));

        assertEquals(200, response.getStatus());
        assertEquals("value", response.readEntity(String.class));
    }

    @Test
    public void testInflectorMethodInstanceClassNegative() throws Exception {
        final Response response = target("methodInstanceClass").request().post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));

        assertEquals(500, response.getStatus());
    }

    @Test
    public void testInflectorMethodClassClass() throws Exception {
        final Response response = target("methodClassClass").request().post(Entity.entity("value", MediaType.TEXT_PLAIN_TYPE));

        assertEquals(200, response.getStatus());
        assertEquals("value", response.readEntity(String.class));
    }

    @Test
    public void testInflectorMethodClassClassNegative() throws Exception {
        final Response response = target("methodClassClass").request().post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));

        assertEquals(500, response.getStatus());
    }

    @Test
    public void testInflectorMethodInstanceInterface() throws Exception {
        final Response response = target("methodInstanceInterface").request()
                .post(Entity.entity("value", MediaType.TEXT_PLAIN_TYPE));

        assertEquals(200, response.getStatus());
        assertEquals("value", response.readEntity(String.class));
    }

    @Test
    public void testInflectorMethodInstanceInterfaceNegative() throws Exception {
        final Response response = target("methodInstanceInterface").request()
                .post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));

        assertEquals(500, response.getStatus());
    }

    @Test
    public void testInflectorMethodClassInterface() throws Exception {
        final Response response = target("methodClassInterface").request()
                .post(Entity.entity("value", MediaType.TEXT_PLAIN_TYPE));

        assertEquals(200, response.getStatus());
        assertEquals("value", response.readEntity(String.class));
    }

    @Test
    public void testInflectorMethodClassInterfaceNegative() throws Exception {
        final Response response = target("methodClassInterface").request().post(Entity.entity(null, MediaType.TEXT_PLAIN_TYPE));

        assertEquals(500, response.getStatus());
    }
}
