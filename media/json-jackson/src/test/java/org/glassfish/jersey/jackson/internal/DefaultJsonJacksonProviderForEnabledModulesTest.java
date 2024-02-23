/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson.internal;

import org.glassfish.jersey.jackson.internal.model.ServiceTest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DefaultJsonJacksonProviderForEnabledModulesTest extends JerseyTest {
    @Override
    protected final Application configure() {
        return new ResourceConfig(ServiceTest.class)
                .property("jersey.config.json.jackson.enabled.modules", "jackson-module-kotlin");
    }

    @Test
    public final void testDisabledModule() {
        try (Response response = target("entity/simple").request().get()) {
            assertEquals(400, response.getStatus());
            assertNotEquals("{\"name\":\"Hello\",\"value\":\"World\"}", response.readEntity(String.class));
        }
    }

}
