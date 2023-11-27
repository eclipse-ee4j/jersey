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

module org.glassfish.jersey.core.client {
    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.ws.rs;

    requires java.logging;

    requires org.glassfish.jersey.core.common;

    exports org.glassfish.jersey.client;
    exports org.glassfish.jersey.client.authentication;
    exports org.glassfish.jersey.client.filter;
    exports org.glassfish.jersey.client.http;
    exports org.glassfish.jersey.client.inject;
    exports org.glassfish.jersey.client.spi;
    exports org.glassfish.jersey.client.internal;

    exports org.glassfish.jersey.client.innate to
            org.glassfish.jersey.apache.connector,
            org.glassfish.jersey.netty.connector,
            org.glassfish.jersey.grizzly.connector,
            org.glassfish.jersey.jetty.connector;

    exports org.glassfish.jersey.client.innate.http to
            org.glassfish.jersey.apache.connector,
            org.glassfish.jersey.netty.connector,
            org.glassfish.jersey.grizzly.connector,
            org.glassfish.jersey.jetty.connector,
            org.glassfish.jersey.jdk.connector;

    opens org.glassfish.jersey.client;
    opens org.glassfish.jersey.client.spi;
    opens org.glassfish.jersey.client.filter;

    // for Localization messages
    opens org.glassfish.jersey.client.internal;

    uses org.glassfish.jersey.client.spi.AsyncConnectorCallback;
    uses org.glassfish.jersey.client.spi.ConnectorProvider;
    uses org.glassfish.jersey.client.spi.DefaultSslContextProvider;
    uses org.glassfish.jersey.client.spi.InvocationBuilderListener;
    uses org.glassfish.jersey.client.spi.PostInvocationInterceptor;
    uses org.glassfish.jersey.client.spi.PreInvocationInterceptor;

    provides jakarta.ws.rs.client.ClientBuilder
            with org.glassfish.jersey.client.JerseyClientBuilder;
}