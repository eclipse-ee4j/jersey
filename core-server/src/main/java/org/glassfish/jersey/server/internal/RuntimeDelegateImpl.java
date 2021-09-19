/*
 * Copyright (c) 2011, 2021 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.core.Application;

import jakarta.ws.rs.core.EntityPart;
import org.glassfish.jersey.internal.AbstractRuntimeDelegate;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.WebServerFactory;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spi.WebServer;

/**
 * Server-side implementation of JAX-RS {@link jakarta.ws.rs.ext.RuntimeDelegate}.
 * This overrides the default implementation of
 * {@link jakarta.ws.rs.ext.RuntimeDelegate} from jersey-common which does not
 * implement
 * {@link #createEndpoint(jakarta.ws.rs.core.Application, java.lang.Class)}
 * method.
 *
 * @author Jakub Podlesak
 * @author Marek Potociar
 * @author Martin Matula
 */
public class RuntimeDelegateImpl extends AbstractRuntimeDelegate {

    public RuntimeDelegateImpl() {
        super(new MessagingBinders.HeaderDelegateProviders().getHeaderDelegateProviders());
    }

    @Override
    public <T> T createEndpoint(final Application application, final Class<T> endpointType)
            throws IllegalArgumentException, UnsupportedOperationException {
        if (application == null) {
            throw new IllegalArgumentException("application is null.");
        }
        return ContainerFactory.createContainer(endpointType, application);
    }

    @Override
    public JerseySeBootstrapConfiguration.Builder createConfigurationBuilder() {
        return JerseySeBootstrapConfiguration.builder();
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<SeBootstrap.Instance> bootstrap(final Application application,
            final SeBootstrap.Configuration configuration) {
        final JerseySeBootstrapConfiguration jerseySeConfiguration = JerseySeBootstrapConfiguration.from(configuration);
        return CompletableFuture.supplyAsync(() -> {
            final Class<WebServer> httpServerClass = configuration.hasProperty(ServerProperties.WEBSERVER_CLASS)
                    ? (Class<WebServer>) configuration.property(ServerProperties.WEBSERVER_CLASS)
                    : WebServer.class;


            return new SeBootstrap.Instance() {
                private final WebServer webServer =
                        WebServerFactory.createServer(httpServerClass, application, jerseySeConfiguration);

                @Override
                public final JerseySeBootstrapConfiguration configuration() {
                    return JerseySeBootstrapConfiguration.from(name -> {
                        switch (name) {
                        case SeBootstrap.Configuration.PORT:
                            return webServer.port();
                        case ServerProperties.WEBSERVER_CLASS:
                            return webServer.getClass();
                        default:
                            return configuration.property(name);
                        }
                    });
                }

                @Override
                public final CompletionStage<StopResult> stop() {
                    return this.webServer.stop().thenApply(nativeResult -> new StopResult() {

                        @Override
                        public final <T> T unwrap(final Class<T> nativeClass) {
                            return nativeClass.cast(nativeResult);
                        }
                    });
                }

                @Override
                public final <T> T unwrap(final Class<T> nativeClass) {
                    return nativeClass.isInstance(this.webServer) ? nativeClass.cast(this.webServer)
                            : this.webServer.unwrap(nativeClass);
                }
            };
        });
    }
}
