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

module org.glasfish.jersey.gf.ejb {
    requires java.logging;
    requires java.naming;

    requires jakarta.ws.rs;
    requires static jakarta.activation;
    requires jakarta.annotation;
    requires jakarta.ejb.api;
    requires jakarta.inject;
    requires jakarta.interceptor.api;

    requires ejb.container;
    requires internal.api;
    requires config.api;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.ext.cdi1x;
    requires org.glassfish.jersey.inject.hk2;


    exports org.glassfish.jersey.gf.ejb.internal;
    opens org.glassfish.jersey.gf.ejb.internal;
}