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

module org.glassfish.jersey.netty.connector {
    requires java.logging;

    requires jakarta.ws.rs;
    requires jakarta.inject;

    requires io.netty.all;
    requires io.netty.buffer;
    requires io.netty.handler;
    requires io.netty.handler.proxy;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.codec.http2;
    requires io.netty.common;
    requires io.netty.resolver;
    requires io.netty.transport;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.netty.connector;
    exports org.glassfish.jersey.netty.connector.internal;

    opens org.glassfish.jersey.netty.connector;
    opens org.glassfish.jersey.netty.connector.internal;
}