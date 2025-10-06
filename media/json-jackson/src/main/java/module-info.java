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

module org.glassfish.jersey.media.json.jackson {
    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.ws.rs;
    requires java.logging;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires static com.fasterxml.jackson.module.jaxb;
    requires static com.fasterxml.jackson.module.jakarta.xmlbind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.ext.entity.filtering;

    exports org.glassfish.jersey.jackson;
    exports org.glassfish.jersey.jackson.internal;
    exports org.glassfish.jersey.jackson.internal.jackson.jaxrs.annotation;
    exports org.glassfish.jersey.jackson.internal.jackson.jaxrs.base;
    exports org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg;
    exports org.glassfish.jersey.jackson.internal.jackson.jaxrs.json;
    exports org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.annotation;
    exports org.glassfish.jersey.jackson.internal.jackson.jaxrs.util;

    opens org.glassfish.jersey.jackson;
    opens org.glassfish.jersey.jackson.internal;
    opens org.glassfish.jersey.jackson.internal.jackson.jaxrs.annotation;
    opens org.glassfish.jersey.jackson.internal.jackson.jaxrs.base;
    opens org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg;
    opens org.glassfish.jersey.jackson.internal.jackson.jaxrs.json;
    opens org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.annotation;
    opens org.glassfish.jersey.jackson.internal.jackson.jaxrs.util;

    provides org.glassfish.jersey.internal.spi.AutoDiscoverable with
            org.glassfish.jersey.jackson.internal.JacksonAutoDiscoverable;

}