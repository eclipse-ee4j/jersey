/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon;

import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.JerseySeBootstrapConfiguration;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.WebServer;

import javax.net.ssl.SSLParameters;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public final class HelidonHttpServer implements WebServer {

    private final HelidonHttpContainer helidonHttpContainer;

    HelidonHttpServer(Application application) {
        this(HelidonHttpContainerBuilder.builder().application(application), JerseySeBootstrapConfiguration.builder().build());
    }

    HelidonHttpServer(Application application, JerseySeBootstrapConfiguration seBootstrapConfig) {
        this(HelidonHttpContainerBuilder.builder().application(application), seBootstrapConfig);
    }

    HelidonHttpServer(Class<? extends Application> applicationClass, JerseySeBootstrapConfiguration seBootstrapConfig) {
        this(HelidonHttpContainerBuilder.builder().applicationClass(applicationClass), seBootstrapConfig);
    }

    HelidonHttpServer(final HelidonHttpContainerBuilder builder, JerseySeBootstrapConfiguration seBootstrapConfig) {
        builder.uri(seBootstrapConfig.uri(false));
        if (seBootstrapConfig.isHttps()) {
            builder.sslContext(seBootstrapConfig.sslContext());
            if (seBootstrapConfig.sslClientAuthentication() != null) {
                final SSLParameters params = new SSLParameters();
                switch (seBootstrapConfig.sslClientAuthentication()) {
                    case OPTIONAL:
                        params.setWantClientAuth(true);
                        break;
                    case MANDATORY:
                        params.setNeedClientAuth(true);
                        break;
                }
                builder.sslParameters(params);
            }
        }
        this.helidonHttpContainer = builder.build();
    }

    @Override
    public Container container() {
        return helidonHttpContainer;
    }

    @Override
    public int port() {
        return helidonHttpContainer.port();
    }

    @Override
    public CompletionStage<?> start() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.helidonHttpContainer.start();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public CompletionStage<?> stop() {
        return CompletableFuture.runAsync(() -> {
            try {
                this.helidonHttpContainer.stop();
            } catch (final Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    @Override
    public <T> T unwrap(Class<T> nativeClass) {
        return nativeClass.cast(helidonHttpContainer.unwrap());
    }
}
