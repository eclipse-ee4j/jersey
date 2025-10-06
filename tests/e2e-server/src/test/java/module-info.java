/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

open module org.glassfish.jersey.tests.e2e.server.test {
    requires com.fasterxml.jackson.annotation;
    requires com.google.gson;
    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.servlet;
    requires jakarta.validation;
    requires jakarta.ws.rs;
    requires jakarta.xml.bind;
    requires java.logging;
    requires java.management;
    requires java.xml;
    requires org.eclipse.jetty.ee10.servlet;
    requires org.eclipse.jetty.server;
    requires org.eclipse.persistence.moxy;
    requires org.glassfish.grizzly.http.server;
    requires org.glassfish.jersey.apache5.connector;
    requires org.glassfish.jersey.container.jetty.http;
    requires org.glassfish.jersey.container.servlet;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.ext.bean.validation;
    requires org.glassfish.jersey.ext.mvc;
    requires org.glassfish.jersey.ext.mvc.bean.validation;
    requires org.glassfish.jersey.ext.mvc.freemarker;
    requires org.glassfish.jersey.ext.mvc.mustache;
    requires org.glassfish.jersey.grizzly.connector;
    requires org.glassfish.jersey.inject.hk2;
    requires org.glassfish.jersey.media.json.gson;
    requires org.glassfish.jersey.media.json.jackson;
    requires org.glassfish.jersey.media.json.jettison;
    requires org.glassfish.jersey.media.jsonb;
    requires org.glassfish.jersey.media.moxy;
    requires org.glassfish.jersey.media.multipart;
    requires org.glassfish.jersey.tests.framework.core;
    requires org.glassfish.jersey.tests.framework.provider.grizzly;
    requires org.glassfish.jersey.tests.framework.provider.jetty;
    requires org.hibernate.validator;
    requires org.junit.jupiter.api;
    requires org.junit.platform.suite.api;
    requires org.hamcrest;
    requires org.xmlunit;
    requires org.glassfish.grizzly.http;
    requires org.glassfish.jersey.test.framework.provider.inmemory;
    requires org.glassfish.jersey.test.framework.provider.jdk.http;

    provides jakarta.ws.rs.ext.MessageBodyReader with
            org.glassfish.jersey.tests.e2e.server.AbstractDisableMetainfServicesLookupTest.UselessMessageProvider;
    provides jakarta.ws.rs.ext.MessageBodyWriter with
            org.glassfish.jersey.tests.e2e.server.AbstractDisableMetainfServicesLookupTest.UselessMessageProvider;
    provides org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable with
            org.glassfish.jersey.tests.e2e.server.monitoring.ApplicationInfoTest.ForcedAutoDiscoverableImpl;
}