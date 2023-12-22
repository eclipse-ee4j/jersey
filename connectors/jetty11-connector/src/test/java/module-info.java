/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

module org.glassfish.jersey.jetty11.connector {
    requires java.logging;

    requires jakarta.inject;
    requires jakarta.ws.rs;

    requires org.eclipse.jetty.client;
//    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.http;
    requires org.eclipse.jetty.io;
    requires org.eclipse.jetty.util;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.hamcrest;

    //  required by Grizzly* modules
    requires pfl.tf;
    requires gmbal;
//    end of required by Grizzly* modules

    requires jakarta.xml.bind;

    requires org.glassfish.jersey.tests.framework.core;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.jetty.connector;
    opens org.glassfish.jersey.jetty.connector;
}