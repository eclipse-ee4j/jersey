/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved.
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

module org.glassfish.jersey.tests.integration.cdi.singleton.test {
    requires jakarta.el;
    requires jakarta.inject;
    requires jakarta.ws.rs;

    requires org.glassfish.jersey.inject.hk2;
    requires org.glassfish.jersey.container.grizzly2.http;
    requires org.glassfish.jersey.ext.weld2.se;
    requires org.glassfish.jersey.tests.integration.cdi.singleton;

    requires weld.core.impl;
    requires weld.environment.common;
    requires weld.se.core;

    requires org.glassfish.jersey.tests.framework.core;
    requires org.glassfish.jersey.tests.framework.provider.external;
    requires org.glassfish.jersey.tests.framework.provider.grizzly;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.platform.engine;

    exports org.glassfish.jersey.tests.cdi.singleton.test;
}