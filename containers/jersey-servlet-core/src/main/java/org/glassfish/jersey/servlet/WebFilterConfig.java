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
 * A filter based web config. Delegates all invocations to the filter
 * configuration from the servlet api.
 *
 * @author Paul Sandoz
 * @author Guilherme Silveira
 */
public final class WebFilterConfig implements WebConfig {

    private final FilterConfig filterConfig;

    public WebFilterConfig(final FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public WebConfig.ConfigType getConfigType() {
        return WebConfig.ConfigType.FilterConfig;
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public FilterConfig getFilterConfig() {
        return filterConfig;
    }

    @Override
    public String getName() {
        return filterConfig.getFilterName();
    }

    @Override
    public String getInitParameter(final String name) {
        return filterConfig.getInitParameter(name);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return filterConfig.getInitParameterNames();
    }

    @Override
    public ServletContext getServletContext() {
        return filterConfig.getServletContext();
    }
}
