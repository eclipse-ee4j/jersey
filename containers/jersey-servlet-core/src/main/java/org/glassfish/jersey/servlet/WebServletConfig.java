/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * A servlet based web config. Delegates all invocations to the servlet
 * configuration from the servlet api.
 *
 * @author Paul Sandoz
 * @author guilherme silveira
 */
public final class WebServletConfig implements WebConfig {

    private final ServletContainer servlet;

    public WebServletConfig(final ServletContainer servlet) {
        this.servlet = servlet;
    }

    @Override
    public WebConfig.ConfigType getConfigType() {
        return WebConfig.ConfigType.ServletConfig;
    }

    @Override
    public ServletConfig getServletConfig() {
        return servlet.getServletConfig();
    }

    @Override
    public FilterConfig getFilterConfig() {
        return null;
    }

    @Override
    public String getName() {
        return servlet.getServletName();
    }

    @Override
    public String getInitParameter(final String name) {
        return servlet.getInitParameter(name);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return servlet.getInitParameterNames();
    }

    @Override
    public ServletContext getServletContext() {
        return servlet.getServletContext();
    }
}
