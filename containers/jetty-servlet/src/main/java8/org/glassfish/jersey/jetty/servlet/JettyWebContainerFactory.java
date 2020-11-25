/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jetty.servlet;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.Servlet;

import jakarta.ws.rs.ProcessingException;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.internal.LocalizationMessages;

/**
 * Jersey {@code Server} stub based on Jetty {@link org.eclipse.jetty.server.Server}.
 * <p>
 * For JDK 1.8 only since Jetty 11 does not support JDKs below 11
 */
public final class JettyWebContainerFactory {

    private JettyWebContainerFactory() {
    }


    public static Server create(String u)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    public static Server create(String u, Map<String, String> initParams)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    public static Server create(URI u)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    public static Server create(URI u, Map<String, String> initParams)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    public static Server create(String u, Class<? extends Servlet> c)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    public static Server create(String u, Class<? extends Servlet> c,
                                Map<String, String> initParams)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    public static Server create(URI u, Class<? extends Servlet> c)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    public static Server create(URI u, Class<? extends Servlet> c, Map<String, String> initParams)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    private static Server create(URI u, Class<? extends Servlet> c, Servlet servlet,
                                 Map<String, String> initParams, Map<String, String> contextInitParams)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }

    public static Server create(URI u, Servlet servlet, Map<String, String> initParams, Map<String, String> contextInitParams)
            throws Exception {
        throw new ProcessingException(LocalizationMessages.NOT_SUPPORTED());
    }
}