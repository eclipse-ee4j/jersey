/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

module org.glassfish.jersey.ext.weld2.se {
    requires jakarta.inject;
    requires jakarta.cdi;
    requires jakarta.ws.rs;

    requires weld.api;
    requires weld.se.core;

    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.ext.cdi1x;
    requires org.glassfish.jersey.inject.hk2;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;

    exports org.glassfish.jersey.weld.se to
            org.glassfish.jersey.core.common,
            weld.core.impl;
    opens org.glassfish.jersey.weld.se to
            org.glassfish.jersey.core.common,
            weld.core.impl;

    exports org.glassfish.jersey.tests.weld;
    opens org.glassfish.jersey.tests.weld;

    provides org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore with
            org.glassfish.jersey.weld.se.WeldInjectionManagerStore;
    provides org.glassfish.jersey.server.spi.ExternalRequestScope with
            org.glassfish.jersey.weld.se.WeldRequestScope;
}
