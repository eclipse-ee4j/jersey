/*
 * Copyright (c) 2022, 2025 Oracle and/or its affiliates. All rights reserved.
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

module org.glassfish.jersey.container.jetty.http {
    requires java.logging;

    requires jakarta.ws.rs;
    requires jakarta.inject;

    requires org.eclipse.jetty.http;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.security;
    requires org.eclipse.jetty.util;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.jetty;
    exports org.glassfish.jersey.jetty.internal; // localization
    opens org.glassfish.jersey.jetty;
    opens org.glassfish.jersey.jetty.internal;

    provides org.glassfish.jersey.server.spi.ContainerProvider with
            org.glassfish.jersey.jetty.JettyHttpContainerProvider;
    provides org.glassfish.jersey.server.spi.WebServerProvider with
            org.glassfish.jersey.jetty.JettyHttpServerProvider;
}