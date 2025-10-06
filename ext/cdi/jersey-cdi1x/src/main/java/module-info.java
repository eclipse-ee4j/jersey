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

module org.glassfish.jersey.ext.cdi1x {

    requires java.naming;
    requires java.logging;

    requires jakarta.cdi;
    requires jakarta.inject;
    requires jakarta.annotation;
    requires jakarta.ws.rs;

    requires org.glassfish.hk2.api;

    requires static org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.inject.hk2;

    exports org.glassfish.jersey.ext.cdi1x.internal; // @JerseyVetoed
    exports org.glassfish.jersey.ext.cdi1x.internal.spi;
    exports org.glassfish.jersey.ext.cdi1x.spi;

    opens org.glassfish.jersey.ext.cdi1x.internal;
    opens org.glassfish.jersey.ext.cdi1x.internal.spi;
    opens org.glassfish.jersey.ext.cdi1x.spi;

    uses org.glassfish.jersey.ext.cdi1x.spi.Hk2CustomBoundTypesProvider;
    uses org.glassfish.jersey.ext.cdi1x.internal.spi.BeanManagerProvider;
    uses org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerInjectedTarget;
    uses org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore;
    uses org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionTargetListener;

    provides jakarta.enterprise.inject.spi.Extension with
            org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider,
            org.glassfish.jersey.ext.cdi1x.internal.ProcessAllAnnotatedTypes;
//    TODO Cannot do with requires static
//    provides org.glassfish.jersey.server.spi.ComponentProvider with
//            org.glassfish.jersey.ext.cdi1x.internal.CdiServerComponentProvider;
    provides org.glassfish.jersey.spi.ComponentProvider with
            org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
}