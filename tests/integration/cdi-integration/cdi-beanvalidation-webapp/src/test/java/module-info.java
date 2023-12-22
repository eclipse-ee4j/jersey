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

module org.glassfish.jersey.tests.integration.cdi.beanvalidation.webapp.test {
    requires jakarta.cdi;
    requires jakarta.inject;
    requires jakarta.validation;
    requires jakarta.ws.rs;

    requires weld.se.core;
    requires weld.core.impl;
    requires org.glassfish.grizzly.http.server;
    requires org.hibernate.validator;
    requires org.hibernate.validator.cdi;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.inject.hk2;
    requires org.glassfish.jersey.container.grizzly2.http;
    requires org.glassfish.jersey.ext.weld2.se;

    requires org.glassfish.jersey.tests.integration.cdi.beanvalidation.webapp;

    requires org.junit.jupiter.api;
    requires org.hamcrest;

    requires org.glassfish.jersey.tests.framework.core;
    requires org.glassfish.jersey.tests.framework.provider.external;
    requires org.glassfish.jersey.tests.framework.provider.grizzly;

    exports org.glassfish.jersey.tests.cdi.bv.test;
    opens org.glassfish.jersey.tests.cdi.bv.test;
}