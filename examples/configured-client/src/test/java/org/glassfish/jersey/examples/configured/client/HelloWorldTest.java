/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.configured.client;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.glassfish.jersey.examples.configured.client.App.ENTITY_PROPERTY;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HelloWorldTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        // mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory
        // mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory
        // mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.jdkhttp.JdkHttpServerTestContainerFactory
        // mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.simple.SimpleTestContainerFactory
        enable(TestProperties.LOG_TRAFFIC);
        // enable(TestProperties.DUMP_ENTITY);
        return new ResourceConfig(HelloWorldResource.class);
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    @ResourceLock(value = "dummy", mode = ResourceAccessMode.READ)
    public void testEntity() {
        WebTarget target = target("helloworld");
        Object entity = target.getConfiguration().getProperty(ENTITY_PROPERTY);
        try (Response response = target.request().post(Entity.entity(entity, MediaType.TEXT_PLAIN_TYPE))) {
            Assertions.assertEquals(200, response.getStatus());
            String readEntity = response.readEntity(String.class);
            System.out.println(entity);
            Assertions.assertEquals(entity, readEntity);
        }
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    @ResourceLock(value = "dummy", mode = ResourceAccessMode.READ)
    public void testConnector() {
        try (Response response = target("helloworld").path("agent").request().get()) {
            Assertions.assertEquals(200, response.getStatus());
            String entity = response.readEntity(String.class);
            System.out.println(entity);
            Assertions.assertTrue(entity.contains("Apache HttpClient 5"));
        }
    }
}
