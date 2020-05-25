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
 * The Web configuration for accessing initialization parameters of a Web
 * component and the {@link ServletContext}.
 *
 * @author Paul Sandoz
 */
public interface WebConfig {

    /**
     * The web configuration type.
     */
    public static enum ConfigType {
        /**
         * A configuration type of servlet configuration.
         */
        ServletConfig,
        /**
         * A configuration type of filter configuration.
         */
        FilterConfig
    }

    /**
     * Get the configuration type of this config.
     *
     * @return the configuration type.
     */
    ConfigType getConfigType();

    /**
     * Get the corresponding ServletConfig if this WebConfig represents a {@link ServletConfig}
     *
     * @return servlet config or null
     */
    ServletConfig getServletConfig();

    /**
     * Get the corresponding FilterConfig if this WebConfig represents a {@link FilterConfig}
     *
     * @return filter config or null
     */
    FilterConfig getFilterConfig();

    /**
     * Get the name of the Web component.
     *
     * @return the name of the Web component.
     */
    String getName();

    /**
     * Get an initialization parameter.
     *
     * @param name the parameter name.
     * @return the parameter value, or null if the parameter is not present.
     */
    String getInitParameter(String name);

    /**
     * Get the enumeration of initialization parameter names.
     *
     * @return the enumeration of initialization parameter names.
     */
    Enumeration getInitParameterNames();

    /**
     * Get the {@link ServletContext}.
     *
     * @return the {@link ServletContext}.
     */
    ServletContext getServletContext();
}
