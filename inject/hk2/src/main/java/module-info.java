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

import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.internal.inject.InjectionManagerFactory;

module org.glassfish.jersey.inject.hk2 {

    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.ws.rs;

    requires java.logging;

   //HK2 is not yet modularized
    requires org.glassfish.hk2.api;
    requires org.glassfish.hk2.locator;
    requires org.glassfish.hk2.utilities;

    requires org.glassfish.jersey.core.common;

    exports org.glassfish.jersey.inject.hk2;
    opens org.glassfish.jersey.inject.hk2;

    provides InjectionManagerFactory
            with Hk2InjectionManagerFactory;
}