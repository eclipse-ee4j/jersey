/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.ext.cdi1x.internal.spi.BeanManagerProvider;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerInjectedTarget;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionManagerStore;
import org.glassfish.jersey.ext.cdi1x.internal.spi.InjectionTargetListener;
import org.glassfish.jersey.ext.cdi1x.spi.Hk2CustomBoundTypesProvider;

module org.glassfish.jersey.ext.cdi1x {

    requires java.naming;
    requires java.logging;

    requires jakarta.cdi;
    requires jakarta.annotation;

    requires org.glassfish.hk2.api;

    requires transitive org.glassfish.jersey.core.server;
    requires transitive org.glassfish.jersey.core.common;
    requires transitive org.glassfish.jersey.inject.hk2;

    exports org.glassfish.jersey.ext.cdi1x.internal;
    exports org.glassfish.jersey.ext.cdi1x.internal.spi;
    exports org.glassfish.jersey.ext.cdi1x.spi;
    opens org.glassfish.jersey.ext.cdi1x.internal;

    uses Hk2CustomBoundTypesProvider;
    uses BeanManagerProvider;
    uses InjectionManagerInjectedTarget;
    uses InjectionManagerStore;
    uses InjectionTargetListener;
}