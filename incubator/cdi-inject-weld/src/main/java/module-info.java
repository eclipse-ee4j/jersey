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

module org.glassfish.jersey.incubator.cdi.inject.weld {
    requires jakarta.annotation;
    requires jakarta.cdi;
    requires jakarta.inject;
    requires jakarta.interceptor;
    requires jakarta.ws.rs;

    requires java.logging;

    requires weld.api;
    requires weld.core.impl;
    requires weld.spi;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.inject.weld.internal.managed to
            org.glassfish.jersey.core.common,
            weld.core.impl;
    exports org.glassfish.jersey.inject.weld.managed to org.glassfish.jersey.core.common;

    opens org.glassfish.jersey.inject.weld.internal.managed to weld.core.impl;
    opens org.glassfish.jersey.inject.weld.internal.scope to weld.core.impl;
    opens org.glassfish.jersey.inject.weld.managed to weld.core.impl;
    opens org.glassfish.jersey.inject.weld.internal.injector to weld.core.impl;

    uses org.glassfish.jersey.innate.BootstrapPreinitialization;

    provides jakarta.enterprise.inject.spi.Extension with
            org.glassfish.jersey.inject.weld.internal.managed.BinderRegisterExtension;

    provides org.glassfish.jersey.internal.inject.InjectionManagerFactory with
            org.glassfish.jersey.inject.weld.managed.CdiInjectionManagerFactory;
}