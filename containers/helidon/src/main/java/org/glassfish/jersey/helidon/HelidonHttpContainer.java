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

import io.helidon.common.context.Context;
import io.helidon.common.tls.Tls;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;

/**
 * {@link Container} running on the Helidon {@link WebServer} core, supporting Jersey routing
 *
 * @since 3.1.11
 */
public final class HelidonHttpContainer implements Container, WebServer {

    private WebServer webServer;

    private ApplicationHandler applicationHandler;

    HelidonHttpContainer(Application application, HelidonJerseyBridge bridge) {
        this.applicationHandler = new ApplicationHandler(application,
                new HelidonHttpContainerBinder(), bridge.getParentContext());
        this.webServer = bridge.getBuilder().build();
        bridge.setContainer(this);
    }

    HelidonHttpContainer(Class<? extends Application> applicationClass, HelidonJerseyBridge bridge) {
        this.applicationHandler = new ApplicationHandler(applicationClass,
                new HelidonHttpContainerBinder());
        this.webServer = bridge.getBuilder().build();
        bridge.setContainer(this);
    }

    @Override
    public ResourceConfig getConfiguration() {
        return applicationHandler.getConfiguration();
    }

    @Override
    public ApplicationHandler getApplicationHandler() {
        return applicationHandler;
    }

    @Override
    public void reload() {
        reload(new ResourceConfig(getConfiguration()));
    }

    @Override
    public void reload(ResourceConfig configuration) {
        //Helidon container does not support reload
        throw new IllegalStateException(LocalizationMessages.RELOAD_NOT_SUPPORTED());
    }

    @Override
    public WebServer start() {
        webServer.start();
        return this;
    }

    @Override
    public WebServer stop() {
        webServer.stop();
        return this;
    }

    @Override
    public boolean isRunning() {
        return webServer.isRunning();
    }

    @Override
    public int port(String socketName) {
        return webServer.port(socketName);
    }

    @Override
    public Context context() {
        return webServer.context();
    }

    @Override
    public boolean hasTls(String socketName) {
        return webServer.hasTls(socketName);
    }

    @Override
    public void reloadTls(String socketName, Tls tls) {
        webServer.reloadTls(socketName, tls);
    }

    @Override
    public WebServerConfig prototype() {
        return webServer.prototype();
    }

    public io.helidon.webserver.WebServer unwrap() {
        return webServer;
    }
}
