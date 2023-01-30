/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.sebootstrap;

import jakarta.ws.rs.SeBootstrap;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.RuntimeDelegateImpl;
import org.glassfish.jersey.server.spi.Container;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeBootstrapSystemPropertiesTest {

    @BeforeAll
    public static void setUp() {
        System.setProperty(CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER, Boolean.TRUE.toString());
        System.getProperties().put(SeBootstrap.Configuration.PORT, "9998");
        System.getProperties().put(ServerProperties.WEBSERVER_ALLOW_PRIVILEGED_PORTS, Boolean.TRUE.toString());
    }

    @AfterAll
    public static void tearDown() {
        System.clearProperty(CommonProperties.ALLOW_SYSTEM_PROPERTIES_PROVIDER);
        System.clearProperty(SeBootstrap.Configuration.PORT);
        System.clearProperty(ServerProperties.WEBSERVER_ALLOW_PRIVILEGED_PORTS);
    }

    @Test
    public void testDefaultPrivilegedPortSystemProperty() {
        JerseySeBootstrapConfiguration configuration = (JerseySeBootstrapConfiguration) RuntimeDelegateImpl.getInstance()
                .createConfigurationBuilder()
                .port(SeBootstrap.Configuration.DEFAULT_PORT).build();

        URI uri = configuration.uri(true);
        assertEquals(Container.DEFAULT_HTTP_PORT, uri.getPort());
    }

    @Test
    public void testPortSystemProperty() {
        JerseySeBootstrapConfiguration configuration = (JerseySeBootstrapConfiguration) RuntimeDelegateImpl.getInstance()
                .createConfigurationBuilder()
                .build();

        URI uri = configuration.uri(true);
        assertEquals(9998, uri.getPort());
    }
}
