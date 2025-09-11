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

module org.glassfish.jersey.inject.cdi2.se {
    requires java.logging;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.interceptor;
    requires jakarta.ws.rs;

    requires jakarta.cdi;
    requires weld.api;
    requires weld.spi;

    requires weld.core.impl;
    requires weld.se.core;

    requires org.glassfish.jersey.core.common;

    exports org.glassfish.jersey.inject.cdi.se;
    exports org.glassfish.jersey.inject.cdi.se.injector;
    exports org.glassfish.jersey.inject.cdi.se.bean;

    opens org.glassfish.jersey.inject.cdi.se;

    provides org.glassfish.jersey.internal.inject.InjectionManagerFactory with
            org.glassfish.jersey.inject.cdi.se.CdiSeInjectionManagerFactory;
}