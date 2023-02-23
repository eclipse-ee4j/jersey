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

module org.glassfish.jersey.core.server {
    requires jakarta.ws.rs;
    requires static jakarta.xml.bind;
    requires java.logging;
    requires jakarta.annotation;
    requires static java.desktop;
    requires java.management;
    requires jakarta.activation;
    requires jakarta.inject;
    requires jakarta.validation;

    requires jdk.httpserver;

    requires static org.glassfish.jersey.inject.hk2;
    requires static org.glassfish.hk2.api;

    // jersey common modules
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;

    // Exports rather all, which corresponds to previous state without module-info
    exports org.glassfish.jersey.server;
    exports org.glassfish.jersey.server.spi;
    exports org.glassfish.jersey.server.spi.internal;
    exports org.glassfish.jersey.server.model;
    exports org.glassfish.jersey.server.model.internal;
    exports org.glassfish.jersey.server.wadl;
    exports org.glassfish.jersey.server.wadl.config;
    exports org.glassfish.jersey.server.wadl.processor;
    exports org.glassfish.jersey.server.wadl.internal.generators;
    exports org.glassfish.jersey.server.wadl.internal.generators.resourcedoc;
    exports org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model;
    exports org.glassfish.jersey.server.filter;
    exports org.glassfish.jersey.server.filter.internal;
    exports org.glassfish.jersey.server.monitoring;
    exports org.glassfish.jersey.server.wadl.internal;
    exports org.glassfish.jersey.server.internal;
    exports org.glassfish.jersey.server.internal.inject;
    exports org.glassfish.jersey.server.internal.monitoring;
    exports org.glassfish.jersey.server.internal.monitoring.jmx;
    exports org.glassfish.jersey.server.internal.process;
    exports org.glassfish.jersey.server.internal.routing;
    exports org.glassfish.jersey.server.internal.scanning;
    exports org.glassfish.jersey.server.internal.sonar;

    exports com.sun.research.ws.wadl; // to org.glassfish.jersey.core.server.test;
    exports org.glassfish.jersey.server.internal.monitoring.core;

    uses org.glassfish.jersey.server.spi.ComponentProvider;
    uses org.glassfish.jersey.server.spi.ExternalRequestScope;
    uses org.glassfish.jersey.server.spi.WebServerProvider;

    opens com.sun.research.ws.wadl to jakarta.xml.bind;

    opens org.glassfish.jersey.server;
    opens org.glassfish.jersey.server.filter;
    opens org.glassfish.jersey.server.filter.internal;
    opens org.glassfish.jersey.server.internal;
    opens org.glassfish.jersey.server.internal.inject;
    opens org.glassfish.jersey.server.internal.monitoring;
    opens org.glassfish.jersey.server.internal.monitoring.jmx;
    opens org.glassfish.jersey.server.internal.process;
    opens org.glassfish.jersey.server.internal.routing;
    opens org.glassfish.jersey.server.model;
    opens org.glassfish.jersey.server.wadl.processor;

    provides jakarta.ws.rs.ext.RuntimeDelegate
            with org.glassfish.jersey.server.internal.RuntimeDelegateImpl;
    provides org.glassfish.jersey.internal.spi.AutoDiscoverable
            with org.glassfish.jersey.server.filter.internal.ServerFiltersAutoDiscoverable;
    provides org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable
            with org.glassfish.jersey.server.wadl.internal.WadlAutoDiscoverable,
                    org.glassfish.jersey.server.internal.monitoring.MonitoringAutodiscoverable;
    provides org.glassfish.jersey.model.internal.spi.ParameterServiceProvider
            with org.glassfish.jersey.server.model.Parameter.ServerParameterService;
}