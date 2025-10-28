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

    requires static org.glassfish.hk2.api;

    // jersey common modules
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;

    exports org.glassfish.jersey.server;
    exports org.glassfish.jersey.server.filter;
    exports org.glassfish.jersey.server.filter.internal to
            org.glassfish.jersey.core.common,
            org.glassfish.hk2.locator,
            org.glassfish.hk2.utilities,
            org.glassfish.jersey.incubator.cdi.inject.weld;
    exports org.glassfish.jersey.server.internal;
    exports org.glassfish.jersey.server.internal.inject to
            org.glassfish.hk2.locator,
            org.glassfish.jersey.core.common,
            org.glassfish.jersey.core.client,
            org.glassfish.jersey.media.sse,
            org.glassfish.jersey.ext.bean.validation,
            org.glassfish.jersey.media.multipart,
            org.glassfish.jersey.ext.mvc;
    exports org.glassfish.jersey.server.internal.monitoring; // MonitoringFeature
    exports org.glassfish.jersey.server.internal.process to org.glassfish.hk2.locator;
    exports org.glassfish.jersey.server.internal.routing;
    exports org.glassfish.jersey.server.internal.scanning to org.glassfish.jersey.container.servlet;
    exports org.glassfish.jersey.server.internal.sonar;
    exports org.glassfish.jersey.server.model;
    exports org.glassfish.jersey.server.model.internal to org.glassfish.jersey.ext.mvc, org.glassfish.jersey.media.sse;
    exports org.glassfish.jersey.server.monitoring;
    exports org.glassfish.jersey.server.spi;
    exports org.glassfish.jersey.server.spi.internal;
    exports org.glassfish.jersey.server.wadl;
    exports org.glassfish.jersey.server.wadl.config;
    exports org.glassfish.jersey.server.wadl.processor;
    exports org.glassfish.jersey.server.wadl.internal;
    exports org.glassfish.jersey.server.wadl.internal.generators;
    exports org.glassfish.jersey.server.wadl.internal.generators.resourcedoc;
    exports org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model;

    exports com.sun.research.ws.wadl;

    opens com.sun.research.ws.wadl to jakarta.xml.bind;

    opens org.glassfish.jersey.server;
    opens org.glassfish.jersey.server.filter;
    opens org.glassfish.jersey.server.filter.internal to
            org.glassfish.hk2.locator,
            org.glassfish.hk2.utilities,
            org.glassfish.jersey.inject.cdi2.se,
            org.glassfish.jersey.incubator.cdi.inject.weld,
            org.glassfish.jersey.core.client, // NonInjectionManager
            weld.core.impl;
    opens org.glassfish.jersey.server.internal;
    opens org.glassfish.jersey.server.internal.inject to
            org.glassfish.hk2.locator,
            org.glassfish.hk2.utilities,
            org.glassfish.jersey.core.common,
            org.glassfish.jersey.media.sse,
            org.glassfish.jersey.ext.bean.validation,
            org.glassfish.jersey.media.multipart,
            org.glassfish.jersey.ext.mvc;
    opens org.glassfish.jersey.server.internal.monitoring;
    opens org.glassfish.jersey.server.internal.monitoring.jmx to org.glassfish.hk2.utilities;
    opens org.glassfish.jersey.server.internal.process to
            org.glassfish.hk2.locator,
            org.glassfish.hk2.utilities,
            org.glassfish.jersey.inject.cdi2.se,
            org.glassfish.jersey.incubator.cdi.inject.weld,
            weld.core.impl;
    opens org.glassfish.jersey.server.internal.routing;
    opens org.glassfish.jersey.server.internal.sonar;
    opens org.glassfish.jersey.server.model;
    opens org.glassfish.jersey.server.spi;
    opens org.glassfish.jersey.server.spi.internal;
    opens org.glassfish.jersey.server.wadl;
    opens org.glassfish.jersey.server.wadl.config;
    opens org.glassfish.jersey.server.wadl.internal;
    opens org.glassfish.jersey.server.wadl.internal.generators;
    opens org.glassfish.jersey.server.wadl.internal.generators.resourcedoc;
    opens org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model;
    opens org.glassfish.jersey.server.wadl.processor;

    uses org.glassfish.jersey.server.spi.ComponentProvider;
    uses org.glassfish.jersey.server.spi.ExternalRequestScope;
    uses org.glassfish.jersey.server.spi.WebServerProvider;

    provides jakarta.ws.rs.ext.RuntimeDelegate
            with org.glassfish.jersey.server.internal.RuntimeDelegateImpl;
    provides org.glassfish.jersey.internal.spi.AutoDiscoverable
            with org.glassfish.jersey.server.filter.internal.ServerFiltersAutoDiscoverable;
    provides org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable
            with org.glassfish.jersey.server.wadl.internal.WadlAutoDiscoverable,
                    org.glassfish.jersey.server.internal.monitoring.MonitoringAutodiscoverable;
    provides org.glassfish.jersey.model.internal.spi.ParameterServiceProvider
            with org.glassfish.jersey.server.model.Parameter.ServerParameterService;
    provides org.glassfish.jersey.innate.BootstrapPreinitialization with
            org.glassfish.jersey.server.ServerBootstrapPreinitialization;
}