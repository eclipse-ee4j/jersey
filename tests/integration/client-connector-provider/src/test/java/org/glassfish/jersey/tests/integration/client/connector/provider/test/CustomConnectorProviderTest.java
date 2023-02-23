/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.client.connector.provider.test;

import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.glassfish.jersey.tests.integration.client.connector.provider.CustomConnectorProvider;
import org.glassfish.jersey.tests.integration.client.connector.provider.TestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Pavel Bucek
 */
public class CustomConnectorProviderTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Test
    public void testInvoked() {
        Assertions.assertFalse(CustomConnectorProvider.invoked);

        Response response = target().path("test").request("text/plain").get();
        assertEquals(200, response.getStatus());

        assertTrue(CustomConnectorProvider.invoked);
    }
}
