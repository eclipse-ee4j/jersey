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

module org.glassfish.jersey.core.common {
    requires jakarta.ws.rs;
    requires jakarta.inject;
    requires static jakarta.xml.bind;
    requires static jakarta.activation;
    requires jakarta.annotation;
    requires java.logging;
    requires static java.desktop;

    // Filename-based auto-modules
    requires static org.osgi.core;
    requires static osgi.resource.locator;

    exports org.glassfish.jersey;
    exports org.glassfish.jersey.http;
    exports org.glassfish.jersey.internal;
    exports org.glassfish.jersey.internal.config to
            org.glassfish.jersey.core.client,
            org.glassfish.jersey.core.server,
            org.glassfish.jersey.ext.mp.config;
    exports org.glassfish.jersey.internal.guava;
    exports org.glassfish.jersey.internal.inject;
    exports org.glassfish.jersey.internal.l10n;
    exports org.glassfish.jersey.internal.sonar;
    exports org.glassfish.jersey.internal.spi;
    exports org.glassfish.jersey.internal.routing; //
    exports org.glassfish.jersey.internal.util;
    exports org.glassfish.jersey.internal.util.collection;
    exports org.glassfish.jersey.io.spi;
    exports org.glassfish.jersey.logging;
    exports org.glassfish.jersey.message;
    exports org.glassfish.jersey.message.internal; // Providers
    exports org.glassfish.jersey.model;
    exports org.glassfish.jersey.model.internal;
    exports org.glassfish.jersey.model.internal.spi;
    exports org.glassfish.jersey.process;
    exports org.glassfish.jersey.process.internal; // @RequestScoped
    exports org.glassfish.jersey.spi;
    exports org.glassfish.jersey.uri;
    exports org.glassfish.jersey.uri.internal; // JerseyUriBuilder


    exports org.glassfish.jersey.innate to org.glassfish.jersey.core.client,
                                           org.glassfish.jersey.core.server,
                                           org.glassfish.jersey.container.grizzly2.http,
                                           org.glassfish.jersey.container.servlet,
                                           org.glassfish.jersey.container.jetty.http,
                                           org.glassfish.jersey.netty.connector,
                                           org.glassfish.jersey.ext.mp.rest.client,
                                           org.glassfish.jersey.inject.cdi2.se,
                                           org.glassfish.jersey.incubator.cdi.inject.weld;

    exports org.glassfish.jersey.innate.inject to
            org.glassfish.jersey.inject.hk2,
            org.glassfish.jersey.core.client,
            org.glassfish.jersey.core.server,
            org.glassfish.jersey.container.grizzly2.http,
            org.glassfish.jersey.container.servlet,
            org.glassfish.jersey.container.jetty.http,
            org.glassfish.jersey.media.sse,
            org.glassfish.jersey.media.jaxb,
            org.glassfish.jersey.media.json.jackson,
            org.glassfish.jersey.media.moxy,
            org.glassfish.jersey.media.multipart,
            org.glassfish.jersey.ext.bean.validation,
            org.glassfish.jersey.ext.cdi1x,
            org.glassfish.jersey.ext.cdi1x.transaction,
            org.glassfish.jersey.ext.entity.filtering,
            org.glassfish.jersey.ext.metainf.services,
            org.glassfish.jersey.ext.mvc,
            org.glassfish.jersey.incubator.cdi.inject.weld,
            org.glassfish.jersey.incubator.declarative.linking,
            org.glassfish.jersey.inject.cdi2.se,
            org.glassfish.jersey.gf.ejb,
            org.glassfish.jersey.security.oauth1.signature;
    exports org.glassfish.jersey.innate.virtual to org.glassfish.jersey.container.grizzly2.http,
                                                   org.glassfish.jersey.container.jetty.http,
                                                   org.glassfish.jersey.netty.connector,
                                                   org.glassfish.jersey.ext.mp.rest.client;

    opens org.glassfish.jersey.innate.virtual to org.glassfish.jersey.container.grizzly2.http,
                                                 org.glassfish.jersey.container.jetty.http;
    opens org.glassfish.jersey.innate to org.glassfish.jersey.container.servlet;

    exports org.glassfish.jersey.innate.io to org.glassfish.jersey.core.server,
                                              org.glassfish.jersey.core.client,
                                              org.glassfish.jersey.container.servlet,
                                              org.glassfish.jersey.apache5.connector;
    exports org.glassfish.jersey.innate.spi to org.glassfish.jersey.core.client,
                                               org.glassfish.jersey.core.server,
                                               org.glassfish.jersey.media.multipart;
    exports org.glassfish.jersey.innate.inject.spi to org.glassfish.jersey.inject.hk2;

    opens org.glassfish.jersey;
    opens org.glassfish.jersey.http;
    opens org.glassfish.jersey.innate.spi to org.glassfish.jersey.media.multipart;
    opens org.glassfish.jersey.internal;
    opens org.glassfish.jersey.internal.guava;
    opens org.glassfish.jersey.internal.inject;
    opens org.glassfish.jersey.internal.l10n;
    opens org.glassfish.jersey.internal.sonar;
    opens org.glassfish.jersey.internal.spi;
    opens org.glassfish.jersey.internal.routing;
    opens org.glassfish.jersey.internal.util;
    opens org.glassfish.jersey.internal.util.collection;
    opens org.glassfish.jersey.io.spi;
    opens org.glassfish.jersey.logging;
    opens org.glassfish.jersey.message;
    opens org.glassfish.jersey.message.internal;
    opens org.glassfish.jersey.model;
    opens org.glassfish.jersey.model.internal;
    opens org.glassfish.jersey.model.internal.spi;
    opens org.glassfish.jersey.process;
    opens org.glassfish.jersey.process.internal;
    opens org.glassfish.jersey.spi;
    opens org.glassfish.jersey.uri;
    opens org.glassfish.jersey.uri.internal;

    uses jakarta.ws.rs.core.Feature;
    uses jakarta.ws.rs.container.DynamicFeature;
    uses jakarta.ws.rs.ext.RuntimeDelegate;

    uses org.glassfish.jersey.innate.spi.EntityPartBuilderProvider;
    uses org.glassfish.jersey.internal.inject.InjectionManagerFactory;
    uses org.glassfish.jersey.internal.spi.AutoDiscoverable;
    uses org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;
    uses org.glassfish.jersey.model.internal.spi.ParameterServiceProvider;
    uses org.glassfish.jersey.spi.HeaderDelegateProvider;
    uses org.glassfish.jersey.spi.ExternalConfigurationProvider;
    uses org.glassfish.jersey.spi.ComponentProvider;

    provides jakarta.ws.rs.ext.RuntimeDelegate
            with org.glassfish.jersey.internal.RuntimeDelegateImpl;
    provides org.glassfish.jersey.internal.spi.AutoDiscoverable
            with org.glassfish.jersey.logging.LoggingFeatureAutoDiscoverable;
}