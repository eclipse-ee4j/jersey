/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.servlet.init;

import org.glassfish.jersey.servlet.spi.FilterUrlMappingsProvider;

import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provide all configured context paths (url mappings) of the application deployed using filter.
 * <p>
 * The url patterns are returned without the eventual trailing asterisk.
 * <p>
 * The functionality is available in Servlet 3.x environment only, so this
 * implementation of {@link FilterUrlMappingsProvider} interface is Servlet 3 specific.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class FilterUrlMappingsProviderImpl implements FilterUrlMappingsProvider {
    @Override
    public List<String> getFilterUrlMappings(FilterConfig filterConfig) {
        FilterRegistration filterRegistration =
          filterConfig.getServletContext().getFilterRegistration(filterConfig.getFilterName());

        Collection<String> urlPatternMappings = filterRegistration.getUrlPatternMappings();
        List<String> result = new ArrayList<>();

        for (String pattern : urlPatternMappings) {
            result.add(pattern.endsWith("*") ? pattern.substring(0, pattern.length() - 1) : pattern);
        }

        return result;
    }
}
