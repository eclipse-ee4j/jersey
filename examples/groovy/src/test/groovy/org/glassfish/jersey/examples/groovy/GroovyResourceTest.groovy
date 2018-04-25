/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.groovy

import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.glassfish.jersey.test.TestProperties
import org.junit.Test

import javax.ws.rs.core.Response
import static org.junit.Assert.assertEquals

/**
 * Test the availability of the {@link GroovyResource}.
 */
class GroovyResourceTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(GroovyResource.class)
    }

    @Test
    public void testGroovyResource() {
        final Response response = target("groovy").request().get();
        assertEquals("groovy", response.readEntity(String.class));
    }
}
