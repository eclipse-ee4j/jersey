/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

module org.glassfish.jersey.core.server {
    requires transitive jakarta.ws.rs;
    requires static jakarta.xml.bind;
    requires java.logging;
    requires jakarta.annotation;
    requires java.desktop;
    requires java.management;
    requires jakarta.activation;
    requires jakarta.inject;
    requires jakarta.validation;

    requires jdk.httpserver;

    // jersey common modules
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.client;

    // Exports rather all, which corresponds to previous state without module-info
    exports org.glassfish.jersey.server;
    exports org.glassfish.jersey.server.spi;
    exports org.glassfish.jersey.server.spi.internal;
    exports org.glassfish.jersey.server.model;
    exports org.glassfish.jersey.server.wadl;
    exports org.glassfish.jersey.server.wadl.config;
    exports org.glassfish.jersey.server.wadl.processor;
    exports org.glassfish.jersey.server.filter;
    exports org.glassfish.jersey.server.monitoring;
    exports org.glassfish.jersey.server.wadl.internal;
    exports org.glassfish.jersey.server.internal;
    exports org.glassfish.jersey.server.internal.inject;

    provides jakarta.ws.rs.core.Configuration with org.glassfish.jersey.server.ResourceConfig;
//    provides jakarta.ws.rs.core.Configuration with org.glassfish.jersey.server.ServerConfig;

}