/*
 * Copyright (c) 2012, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal;

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test that checks that the right RuntimeDelegateImpl is loaded by JAX-RS.
 *
 * @author Martin Matula
 */
public class RuntimeDelegateImplTest {

    @Test
    public void testCreateEndpoint() {
        RuntimeDelegate delegate = RuntimeDelegate.getInstance();
        try {
            delegate.createEndpoint((Application) null, com.sun.net.httpserver.HttpHandler.class);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException iae) {
            // ok - should be thrown
        } catch (Exception e) {
            fail("IllegalArgumentException should be thrown");
        }
    }

    /**
     * Checks that the right RuntimeDelegateImpl is loaded by JAX-RS.
     */
    @Test
    public void testRuntimeDelegateInstance() {
        try {
            RuntimeDelegate.getInstance().createEndpoint((Application) null, com.sun.net.httpserver.HttpHandler.class);
        } catch (Exception e) {
            // does not matter, this just makes sure the correct RuntimeDelegate is set
        }
        assertSame(RuntimeDelegateImpl.class, RuntimeDelegate.getInstance().getClass());
    }

    @Test
    public final void shouldCreateConfigurationBuilder() {
        // given
        final RuntimeDelegate runtimeDelegate = new RuntimeDelegateImpl();
        // when
        final SeBootstrap.Configuration.Builder configurationBuilder = runtimeDelegate.createConfigurationBuilder();
        // then
        assertThat(configurationBuilder, is(notNullValue()));
    }

    @Test
    public final void shouldBuildDefaultConfiguration() {
        // given
        final SeBootstrap.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();
        // when
        final SeBootstrap.Configuration configuration = configurationBuilder.build();

        // then
        assertThat(configuration, is(notNullValue()));
        assertTrue(configuration.hasProperty(SeBootstrap.Configuration.PROTOCOL));
        assertTrue(configuration.hasProperty(SeBootstrap.Configuration.HOST));
        assertTrue(configuration.hasProperty(SeBootstrap.Configuration.PORT));
        assertTrue(configuration.hasProperty(SeBootstrap.Configuration.ROOT_PATH));
        assertTrue(configuration.hasProperty(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION));
        assertTrue(configuration.hasProperty(SeBootstrap.Configuration.SSL_CONTEXT));
        assertThat(configuration.property(SeBootstrap.Configuration.PROTOCOL), is("HTTP"));
        assertThat(configuration.property(SeBootstrap.Configuration.HOST), is("localhost"));
        assertThat(configuration.property(SeBootstrap.Configuration.PORT), is(SeBootstrap.Configuration.DEFAULT_PORT));
        assertThat(configuration.property(SeBootstrap.Configuration.ROOT_PATH), is("/"));
        assertThat(configuration.property(SeBootstrap.Configuration.SSL_CLIENT_AUTHENTICATION),
                is(SeBootstrap.Configuration.SSLClientAuthentication.NONE));
//        assertThat(configuration.property(SeBootstrap.Configuration.SSL_CONTEXT), is(theInstance(SSLContext.getDefault())));
        assertThat(configuration.protocol(), is("HTTP"));
        assertThat(configuration.host(), is("localhost"));
        assertThat(configuration.port(), is(SeBootstrap.Configuration.DEFAULT_PORT));
        assertThat(configuration.rootPath(), is("/"));
        assertThat(configuration.sslClientAuthentication(), is(SeBootstrap.Configuration.SSLClientAuthentication.NONE));
//        assertThat(configuration.sslContext(), is(theInstance(SSLContext.getDefault())));
    }

    @Test
    public final void shouldBuildConfigurationContainingCustomProperties() {
        // given
        final SeBootstrap.Configuration.Builder configurationBuilder = new RuntimeDelegateImpl().createConfigurationBuilder();
        // when
        final SeBootstrap.Configuration configuration = configurationBuilder.property("property", "value").build();

        assertThat(configuration, is(notNullValue()));
        assertTrue(configuration.hasProperty("property"));
        assertThat(configuration.property("property"), is("value"));
    }
}
