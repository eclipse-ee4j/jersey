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

module org.glassfish.jersey.ext.mp.rest.client {
    requires java.logging;

    requires jakarta.annotation;
    requires jakarta.cdi;
    requires jakarta.inject;
    requires jakarta.interceptor;
    requires jakarta.json;
    requires jakarta.ws.rs;

    requires microprofile.rest.client.api;
    requires microprofile.config.api;
    requires org.reactivestreams;

    requires org.glassfish.jersey.ext.cdi1x;
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.inject.hk2;
    requires org.glassfish.jersey.media.sse;

    exports org.glassfish.jersey.microprofile.restclient;
    exports org.glassfish.jersey.microprofile.restclient.internal; // localization

    opens org.glassfish.jersey.microprofile.restclient;
    opens org.glassfish.jersey.microprofile.restclient.internal;

    provides jakarta.enterprise.inject.spi.Extension with
            org.glassfish.jersey.microprofile.restclient.RestClientExtension;
    provides org.eclipse.microprofile.rest.client.spi.RestClientBuilderResolver with
            org.glassfish.jersey.microprofile.restclient.JerseyRestClientBuilderResolver;
    provides org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable with
            org.glassfish.jersey.microprofile.restclient.RequestHeaderAutoDiscoverable;
}