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

module org.glassfish.jersey.container.servlet {
    requires java.logging;
    requires java.naming;

    requires jakarta.inject;
    requires static jakarta.persistence;
    requires jakarta.ws.rs;
    requires static jakarta.servlet;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;

    exports org.glassfish.jersey.servlet.async;
    exports org.glassfish.jersey.servlet.init;
    exports org.glassfish.jersey.servlet.internal.l10n;
    exports org.glassfish.jersey.servlet.internal.spi; //
    exports org.glassfish.jersey.servlet.spi;
    exports org.glassfish.jersey.servlet;

    opens org.glassfish.jersey.servlet;
    opens org.glassfish.jersey.servlet.internal.l10n;

    uses org.glassfish.jersey.servlet.spi.AsyncContextDelegate;
    uses org.glassfish.jersey.servlet.spi.AsyncContextDelegateProvider;
    uses org.glassfish.jersey.servlet.spi.FilterUrlMappingsProvider;

    provides org.glassfish.jersey.servlet.spi.AsyncContextDelegateProvider with
            org.glassfish.jersey.servlet.async.AsyncContextDelegateProviderImpl;
    provides org.glassfish.jersey.servlet.spi.FilterUrlMappingsProvider with
            org.glassfish.jersey.servlet.init.FilterUrlMappingsProviderImpl;
}