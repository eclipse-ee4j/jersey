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

open module org.glassfish.jersey.container.grizzly2.http {

    requires java.net.http;
    requires java.logging;

    requires jakarta.inject;
    requires jakarta.ws.rs;

    requires org.eclipse.jetty.http2.client;
    requires org.eclipse.jetty.http2.client.transport;

    requires org.bouncycastle.provider;
    requires org.bouncycastle.pkix;
    requires org.eclipse.jetty.client;
    requires org.hamcrest;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;

    requires org.glassfish.grizzly.http.server;
    requires org.glassfish.grizzly;
    requires org.glassfish.grizzly.http;
    requires org.glassfish.grizzly.http2;

    requires org.glassfish.hk2.api;
    requires org.glassfish.hk2.locator;

    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.inject.hk2;

    exports org.glassfish.jersey.grizzly2.httpserver.test.application
            to  org.glassfish.jersey.core.server;
}