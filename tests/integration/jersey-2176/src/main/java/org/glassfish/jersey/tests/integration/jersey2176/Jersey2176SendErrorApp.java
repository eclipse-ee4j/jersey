/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2176;

import javax.ws.rs.ApplicationPath;

/**
 * Configure {@link org.glassfish.jersey.server.ServerProperties#RESPONSE_SET_STATUS_OVER_SEND_ERROR} by {@code true} -
 * method {@link javax.servlet.http.HttpServletResponse#sendError} will be called in case of errors
 * (status {@code 4xx} or {@code 5xx}).
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
@ApplicationPath(Jersey2176SendErrorApp.APP_PATH)
public class Jersey2176SendErrorApp extends Jersey2176App {

    public static final String APP_PATH = "send-error";

    public Jersey2176SendErrorApp() {
        super(false);
    }

}
