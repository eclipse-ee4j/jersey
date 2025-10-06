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

open module org.glassfish.jersey.tests.e2e.entity.test {
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires jakarta.activation;
    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.json;
    requires jakarta.json.bind;
    requires jakarta.ws.rs;
    requires jakarta.xml.bind;
    requires java.desktop;
    requires java.logging;
    requires java.xml;
    requires jettison;
    requires org.eclipse.persistence.moxy;
    requires org.glassfish.grizzly;
    requires org.glassfish.jersey.core.client;
    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.ext.entity.filtering;
    requires org.glassfish.jersey.grizzly.connector;
    requires org.glassfish.jersey.media.jaxb;
    requires org.glassfish.jersey.media.json.jackson;
    requires org.glassfish.jersey.media.json.jettison;
    requires org.glassfish.jersey.media.json.processing;
    requires org.glassfish.jersey.media.jsonb;
    requires org.glassfish.jersey.media.moxy;
    requires org.glassfish.jersey.media.multipart;
    requires org.glassfish.jersey.media.sse;
    requires org.glassfish.jersey.tests.framework.core;
    requires org.hamcrest;
    requires org.junit.jupiter.api;
    requires org.junit.platform.suite.api;

    exports org.glassfish.jersey.tests.e2e.entity;
    exports org.glassfish.jersey.tests.e2e.entity.filtering;
    exports org.glassfish.jersey.tests.e2e.entity.filtering.json;
    exports org.glassfish.jersey.tests.e2e.entity.filtering.domain;
    exports org.glassfish.jersey.tests.e2e.header;
    exports org.glassfish.jersey.tests.e2e.json;
    exports org.glassfish.jersey.tests.e2e.json.entity;
    exports org.glassfish.jersey.tests.e2e.json.entity.pojo;
    exports org.glassfish.jersey.tests.e2e.sse;

}