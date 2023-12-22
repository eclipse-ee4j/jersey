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

module org.glassfish.jersey.tests.framework.provider.grizzly {
    requires java.logging;

    requires jakarta.inject;
    requires jakarta.servlet;
    requires jakarta.ws.rs;

    requires org.glassfish.grizzly;
    requires org.glassfish.grizzly.servlet;
    requires org.glassfish.grizzly.http.server;

    requires org.glassfish.jersey.tests.framework.core;
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.container.grizzly2.http;
    requires org.glassfish.jersey.container.grizzly2.servlet;
    requires org.glassfish.jersey.container.servlet.core;

    exports org.glassfish.jersey.test.grizzly;
    exports org.glassfish.jersey.test.grizzly.pckg;
    exports org.glassfish.jersey.test.grizzly.web;
    exports org.glassfish.jersey.test.grizzly.web.context;
    exports org.glassfish.jersey.test.grizzly.web.ssl;

    opens org.glassfish.jersey.test.grizzly.pckg;
    opens org.glassfish.jersey.test.grizzly.web;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.hamcrest;
}