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

module org.glassfish.jersey.media.json.gson {
    requires com.google.gson;
    requires jakarta.annotation;
    requires jakarta.inject;
    requires jakarta.ws.rs;
    requires org.glassfish.jersey.core.common;

    exports org.glassfish.jersey.gson;
    exports org.glassfish.jersey.gson.internal;
    opens org.glassfish.jersey.gson;
    opens org.glassfish.jersey.gson.internal;

    provides org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable with
            org.glassfish.jersey.gson.internal.JsonGsonAutoDiscoverable;
}