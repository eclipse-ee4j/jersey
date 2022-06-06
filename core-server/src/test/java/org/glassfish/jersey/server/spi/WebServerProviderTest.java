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

package org.glassfish.jersey.server.spi;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.ServerProperties;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletionStage;

public class WebServerProviderTest {
    @Test
    public void testPropertySetsDifferentClass() {
        SeBootstrap.Configuration config =
            SeBootstrap.Configuration.builder().property(ServerProperties.WEBSERVER_CLASS, WebServerTestImpl2.class).build();

        Assert.assertNull(new WebServerProviderTestImpl().createServer(WebServerTestImpl.class, Application.class, config));
    }

    @Test
    public void testPropertySetsCorrectClass() {
        SeBootstrap.Configuration config =
                SeBootstrap.Configuration.builder().property(ServerProperties.WEBSERVER_CLASS, WebServerTestImpl.class).build();

        Assert.assertTrue(
                WebServerTestImpl.class.isInstance(
                        new WebServerProviderTestImpl().createServer(WebServerTestImpl2.class, Application.class, config)
                )
        );
    }

    @Test
    public void testPropertySetsNothingUserTypeIsWrong() {
        SeBootstrap.Configuration config =
                SeBootstrap.Configuration.builder().build();

        Assert.assertNull(new WebServerProviderTestImpl().createServer(WebServerTestImpl2.class, Application.class, config));
    }

    @Test
    public void testPropertySetsNothingUserTypeIsCorrectClass() {
        SeBootstrap.Configuration config =
                SeBootstrap.Configuration.builder().build();

        Assert.assertTrue(
                WebServerTestImpl.class.isInstance(
                        new WebServerProviderTestImpl().createServer(WebServerTestImpl.class, Application.class, config)
                )
        );
    }

    @Test
    public void testPropertySetsNothingUserTypeIsSuperClass() {
        SeBootstrap.Configuration config =
                SeBootstrap.Configuration.builder().build();

        Assert.assertTrue(
                WebServerTestImpl.class.isInstance(
                        new WebServerProviderTestImpl().createServer(WebServer.class, Application.class, config)
                )
        );
    }

    public static class WebServerProviderTestImpl implements WebServerProvider {

        @Override
        public <T extends WebServer> T createServer(
                Class<T> type, Application application, SeBootstrap.Configuration configuration) throws ProcessingException {
            if (WebServerProvider.isSupportedWebServer(WebServerTestImpl.class, type, configuration)) {
                return (T) new WebServerTestImpl();
            }
            return null;
        }

        @Override
        public <T extends WebServer> T createServer(
                Class<T> type,
                Class<? extends Application> applicationClass,
                SeBootstrap.Configuration configuration) throws ProcessingException {
            if (WebServerProvider.isSupportedWebServer(WebServerTestImpl.class, type, configuration)) {
                return (T) new WebServerTestImpl();
            }
            return null;
        }
    }

    public static class WebServerTestImpl implements WebServer {

        @Override
        public Container container() {
            return null;
        }

        @Override
        public int port() {
            return 0;
        }

        @Override
        public CompletionStage<?> start() {
            return null;
        }

        @Override
        public CompletionStage<?> stop() {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> nativeClass) {
            return null;
        }
    }

    public static class WebServerTestImpl2 implements WebServer {

        @Override
        public Container container() {
            return null;
        }

        @Override
        public int port() {
            return 0;
        }

        @Override
        public CompletionStage<?> start() {
            return null;
        }

        @Override
        public CompletionStage<?> stop() {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> nativeClass) {
            return null;
        }
    };
}
