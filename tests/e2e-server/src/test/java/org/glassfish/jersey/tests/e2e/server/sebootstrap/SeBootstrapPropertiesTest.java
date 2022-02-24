/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.RuntimeDelegateImpl;
import org.glassfish.jersey.server.spi.Container;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

public class SeBootstrapPropertiesTest {
    @Test
    public void testRandomPortScanning() {
        JerseySeBootstrapConfiguration configuration = (JerseySeBootstrapConfiguration) RuntimeDelegateImpl.getInstance()
                .createConfigurationBuilder().port(SeBootstrap.Configuration.FREE_PORT).build();

        URI uri = configuration.uri(true);
        Assert.assertTrue(uri.getPort() > 0);
    }

    @Test
    public void testDefaultUnprivilegedPort() {
        JerseySeBootstrapConfiguration configuration = (JerseySeBootstrapConfiguration) RuntimeDelegateImpl.getInstance()
                .createConfigurationBuilder().port(SeBootstrap.Configuration.DEFAULT_PORT).build();

        URI uri = configuration.uri(true);
        Assert.assertEquals(Container.DEFAULT_HTTP_PORT + 8000, uri.getPort());
    }

    @Test
    public void testDefaultPrivilegedPort() {
        JerseySeBootstrapConfiguration configuration = (JerseySeBootstrapConfiguration) RuntimeDelegateImpl.getInstance()
                .createConfigurationBuilder()
                .property(ServerProperties.WEBSERVER_ALLOW_PRIVILEGED_PORTS, Boolean.TRUE)
                .port(SeBootstrap.Configuration.DEFAULT_PORT).build();

        URI uri = configuration.uri(true);
        Assert.assertEquals(Container.DEFAULT_HTTP_PORT, uri.getPort());
    }

    @Test
    public void testDefaultUnprivilegedSecuredPort() {
        JerseySeBootstrapConfiguration configuration = (JerseySeBootstrapConfiguration) RuntimeDelegateImpl.getInstance()
                .createConfigurationBuilder().protocol("HTTPS").port(SeBootstrap.Configuration.DEFAULT_PORT).build();

        URI uri = configuration.uri(true);
        Assert.assertEquals(Container.DEFAULT_HTTPS_PORT + 8000, uri.getPort());
    }

    @Test
    public void testDefaultPrivilegedSecuredPort() {
        JerseySeBootstrapConfiguration configuration = (JerseySeBootstrapConfiguration) RuntimeDelegateImpl.getInstance()
                .createConfigurationBuilder()
                .protocol("HTTPS")
                .property(ServerProperties.WEBSERVER_ALLOW_PRIVILEGED_PORTS, Boolean.TRUE)
                .port(SeBootstrap.Configuration.DEFAULT_PORT).build();

        URI uri = configuration.uri(true);
        Assert.assertEquals(Container.DEFAULT_HTTPS_PORT, uri.getPort());
    }
}
