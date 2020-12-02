/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey4542;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.test.JerseyTest;

import static org.junit.Assert.assertEquals;

import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.jboss.weld.environment.se.Weld;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Bean Validation tests for programmatically created resources.
 *
 * @author Michal Gajdos
 */
public class ProgrammaticValidationTest extends JerseyTest {

    Weld weld;

    @Before
    public void setup() {
        Assume.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
    }

    @Override
    public void setUp() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            if (!ExternalTestContainerFactory.class.isAssignableFrom(getTestContainerFactory().getClass())) {
                weld = new Weld();
                weld.initialize();
            }
            super.setUp();
        }
    }

    @Override
    public void tearDown() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            if (!ExternalTestContainerFactory.class.isAssignableFrom(getTestContainerFactory().getClass())) {
                weld.shutdown();
            }
            super.tearDown();
        }
    }

    @Override
    protected Application configure() {
        final Set<Resource> resources = new HashSet<>();

        Resource.Builder resourceBuilder = Resource.builder("class");
        resourceBuilder
                .addMethod("POST")
                .handledBy(ValidationInflector.class);
        resources.add(resourceBuilder.build());

        return new ResourceConfig().register(LoggingFeature.class).registerResources(resources);
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
}
