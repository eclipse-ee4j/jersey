/*
 *  Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License v. 2.0, which is available at
 *  http://www.eclipse.org/legal/epl-2.0.
 *
 *  This Source Code may also be made available under the following Secondary
 *  Licenses when the conditions for such availability set forth in the
 *  Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 *  version 2 with the GNU Classpath Exception, which is available at
 *  https://www.gnu.org/software/classpath/license.html.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 *
 */

package org.glassfish.jersey.helidon;

import io.helidon.common.tls.Tls;
import io.helidon.common.tls.TlsConfig;
import io.helidon.config.Config;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.spi.Container;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.net.URI;

/**
 * Helidon container builder.
 * Shall be used to create any instance of the {@link HelidonHttpContainer}
 *
 * gives access to the inner {@link WebServerConfig.Builder} which provides possibility of better fine-tune
 * of the container.
 *
 * @since 3.1.11
 */
public class HelidonHttpContainerBuilder {

    private URI baseUri;

    private Application application;

    private Class<? extends Application> applicationClass;

    private String path;

    private Tls tls;

    private final WebServerConfig.Builder webServerBuilder;

    private final HelidonJerseyBridge bridge;

    private SSLParameters sslParameters;

    private SSLContext sslContext;

    private HelidonHttpContainerBuilder() {
        bridge =  new HelidonJerseyBridge();
        webServerBuilder = bridge.getBuilder();
    }

    public WebServerConfig.Builder helidonBuilder() {
        return webServerBuilder;
    }


    public static HelidonHttpContainerBuilder builder() {
        return new HelidonHttpContainerBuilder();
    }

    public HelidonHttpContainerBuilder uri(URI baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    public URI uri() {
        return this.baseUri;
    }

    public HelidonHttpContainerBuilder application(Application application) {
        this.application = application;
        return this;
    }

    public Application application() {
        return this.application;
    }

    public HelidonHttpContainerBuilder applicationClass(Class<? extends Application> applicationClass) {
        this.applicationClass = applicationClass;
        return this;
    }

    public HelidonHttpContainerBuilder path(String path) {
        this.path = path;
        return this;
    }

    public String path() {
        return this.path;
    }

    public HelidonHttpContainerBuilder sslParameters(SSLParameters sslParameters) {
        this.sslParameters = sslParameters;
        return this;
    }

    public SSLParameters sslParameters() {
        return this.sslParameters;
    }

    public HelidonHttpContainerBuilder sslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public SSLContext sslContext() {
        return this.sslContext;
    }

    public HelidonHttpContainerBuilder port(int port) {
        webServerBuilder.port(port);
        return this;
    }

    public HelidonHttpContainerBuilder host(String host) {
        webServerBuilder.host(host);
        return this;
    }

    public HelidonHttpContainerBuilder parentContext(Object parentContext) {
        bridge.setParentContext(parentContext);
        return this;
    }

    public Object parentContext() {
        return bridge.getParentContext();
    }

    public HelidonHttpContainer build() {
        configureBaseUri();
        webServerBuilder.config(Config.create())
                        .routing(configureRouting());
        this.tls = configureTls();
        if (this.tls != null) {
            webServerBuilder.tls(this.tls);
        }
        return (application == null)
               ? new HelidonHttpContainer(applicationClass, bridge) : new HelidonHttpContainer(application, bridge);
    }

    private TlsConfig.Builder addSSLParameterss(TlsConfig.Builder builder) {
        if (sslParameters != null) {
            return builder.sslParameters(sslParameters);
        }
        return builder;
    }

    private TlsConfig.Builder addSSLContext(TlsConfig.Builder builder) {
        if (sslContext != null) {
            return builder.sslContext(sslContext);
        }
        return builder;
    }

    private Tls configureTls() {
        if (this.tls == null
                && (sslParameters != null || sslContext != null)) {
            this.tls = addSSLParameterss(
                    addSSLContext(
                            TlsConfig.builder())
            ).build();
        }
        return this.tls;
    }

    private HttpRouting.Builder configureRouting() {

        final HttpRouting.Builder builder = HttpRouting.builder();
        final HelidonJerseyRoutingService support = HelidonJerseyRoutingService.create(this.bridge);
        if (path != null) {
            builder.register(path, support);
        } else if (baseUri != null && baseUri.getPath() != null) {
            builder.register(baseUri.getPath(), support);
        } else {
            builder.register(support);
        }

        return builder;
    }

    private void configureBaseUri() {
        if (baseUri != null) {
            webServerBuilder
                    .host(baseUri.getHost())
                    .port(baseUri.getPort());
        } else {
            if (webServerBuilder.port() < 0) {
                webServerBuilder.port(Container.DEFAULT_HTTP_PORT);
            }

        }

    }

}
