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

module org.glassfish.jersey.ext.mvc.jsp {
    requires jakarta.inject;
    requires jakarta.servlet;
    requires jakarta.servlet.jsp;
    requires jakarta.ws.rs;

    requires org.glassfish.jersey.core.common;
    requires org.glassfish.jersey.core.server;
    requires org.glassfish.jersey.ext.mvc;

    exports org.glassfish.jersey.server.mvc.jsp;
    exports org.glassfish.jersey.server.mvc.jsp.internal; // localization

    opens org.glassfish.jersey.server.mvc.jsp;
    opens org.glassfish.jersey.server.mvc.jsp.internal;
}