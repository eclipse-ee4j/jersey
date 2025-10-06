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

module org.glassfish.jersey.ext.bean.validation {

    requires java.logging;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.validation;
    requires jakarta.ws.rs;
    requires static jakarta.xml.bind;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;

    requires static org.glassfish.jersey.ext.cdi1x;
    requires static jakarta.cdi;

    exports org.glassfish.jersey.server.validation;
    exports org.glassfish.jersey.server.validation.internal to
            org.glassfish.hk2.locator,
            org.glassfish.hk2.utilities,
            org.glassfish.jersey.core.client,
            org.glassfish.jersey.ext.mvc.bean.validation;
    exports org.glassfish.jersey.server.validation.internal.l10n;
    exports org.glassfish.jersey.server.validation.internal.hibernate to
            org.glassfish.hk2.locator,
            org.glassfish.hk2.utilities,
            org.glassfish.jersey.core.client;
    opens org.glassfish.jersey.server.validation;
    opens org.glassfish.jersey.server.validation.internal to
            org.glassfish.hk2.locator,
            org.glassfish.hk2.utilities,
            org.glassfish.jersey.core.client,
            org.glassfish.jersey.ext.mvc.bean.validation;
    opens org.glassfish.jersey.server.validation.internal.hibernate to
            org.glassfish.hk2.locator,
            org.glassfish.hk2.utilities,
            org.glassfish.jersey.core.client;
    opens org.glassfish.jersey.server.validation.internal.l10n;

    provides org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable with
            org.glassfish.jersey.server.validation.internal.ValidationAutoDiscoverable;
}