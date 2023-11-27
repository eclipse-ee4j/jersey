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

    // Exports rather all, which corresponds to previous state without module-info
    exports org.glassfish.jersey;
    exports org.glassfish.jersey.http;
    exports org.glassfish.jersey.internal;
    exports org.glassfish.jersey.internal.config;
    exports org.glassfish.jersey.internal.guava;
    exports org.glassfish.jersey.internal.inject;
    exports org.glassfish.jersey.internal.jsr166;
    exports org.glassfish.jersey.internal.l10n;
    exports org.glassfish.jersey.internal.sonar;
    exports org.glassfish.jersey.internal.spi;
    exports org.glassfish.jersey.internal.routing;
    exports org.glassfish.jersey.internal.util;
    exports org.glassfish.jersey.internal.util.collection;
    exports org.glassfish.jersey.logging;
    exports org.glassfish.jersey.message;
    exports org.glassfish.jersey.message.internal;
    exports org.glassfish.jersey.model;
    exports org.glassfish.jersey.model.internal;
    exports org.glassfish.jersey.model.internal.spi;
    exports org.glassfish.jersey.process;
    exports org.glassfish.jersey.process.internal;
    exports org.glassfish.jersey.spi;
    exports org.glassfish.jersey.uri;
    exports org.glassfish.jersey.uri.internal;

    exports org.glassfish.jersey.innate.spi to org.glassfish.jersey.media.multipart;

    opens org.glassfish.jersey.innate.spi to org.glassfish.jersey.media.multipart;
    opens org.glassfish.jersey.internal;
    opens org.glassfish.jersey.message.internal;
    opens org.glassfish.jersey.spi;

    uses jakarta.ws.rs.core.Feature;
    uses jakarta.ws.rs.container.DynamicFeature;
    uses jakarta.ws.rs.ext.RuntimeDelegate;

    uses org.glassfish.jersey.innate.spi.EntityPartBuilderProvider;
    uses org.glassfish.jersey.internal.ServiceFinder;
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