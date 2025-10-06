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

open module org.glassfish.jersey.tests.e2e {
    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.servlet;
    requires jakarta.ws.rs;
    requires jakarta.xml.bind;
    requires java.logging;
    requires java.xml;
    requires jdk.security.auth;
    requires org.apache.httpcomponents.client5.httpclient5;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires org.eclipse.jetty.server;
    requires org.glassfish.hk2.api;
    requires org.glassfish.grizzly.http.server;
    requires org.glassfish.jersey.apache5.connector;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.grizzly.connector;
    requires org.glassfish.jersey.inject.hk2;
    requires org.glassfish.jersey.jdk.connector;
    requires org.glassfish.jersey.media.moxy;
    requires org.glassfish.jersey.media.multipart;
    requires org.glassfish.jersey.security.oauth1.client;
    requires org.glassfish.jersey.security.oauth1.server;
    requires org.glassfish.jersey.security.oauth1.signature;
    requires org.glassfish.jersey.security.oauth2.client;
    requires org.glassfish.jersey.test.framework.provider.inmemory;
    requires org.glassfish.jersey.test.framework.provider.jdk.http;
    requires org.glassfish.jersey.test.framework.provider.netty;
    requires org.glassfish.jersey.tests.framework.core;
    requires org.glassfish.jersey.tests.framework.provider.grizzly;
    requires org.glassfish.jersey.tests.framework.provider.jetty;
    requires org.hamcrest;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.junit.platform.suite.api;

    exports org.glassfish.jersey.tests.api;
    exports org.glassfish.jersey.tests.e2e;
    exports org.glassfish.jersey.tests.e2e.common;
    exports org.glassfish.jersey.tests.e2e.client.connector;
    exports org.glassfish.jersey.tests.e2e.container;
    exports org.glassfish.jersey.tests.e2e.inject;
    exports org.glassfish.jersey.tests.e2e.oauth;
    exports org.glassfish.jersey.tests.e2e.sonar;
    exports org.glassfish.jersey.tests.e2e.server.wadl;

}