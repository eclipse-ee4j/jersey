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

module org.glassfish.jersey.jdk.connector {
    requires java.logging;

    requires jakarta.ws.rs;
    requires static jakarta.activation;

    requires osgi.resource.locator;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.client;

    exports org.glassfish.jersey.jdk.connector;
    exports org.glassfish.jersey.jdk.connector.internal to
            org.glassfish.hk2.locator,
            org.glassfish.hk2.utilities;
    exports org.glassfish.jersey.jdk.connector.internal.l10n;

    opens org.glassfish.jersey.jdk.connector;
    opens org.glassfish.jersey.jdk.connector.internal.l10n;
}