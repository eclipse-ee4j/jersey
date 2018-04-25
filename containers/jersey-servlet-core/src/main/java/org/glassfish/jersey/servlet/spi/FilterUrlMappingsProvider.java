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

package org.glassfish.jersey.servlet.spi;

import javax.servlet.FilterConfig;
import java.util.List;

/**
 * Provides an access to context path from the filter configuration.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public interface FilterUrlMappingsProvider {

    /**
     * Return configured context path from the filter configuration.
     *
     * @param filterConfig the {@link FilterConfig} object
     * @returns the {@code List} of url-patterns
     */
    List<String> getFilterUrlMappings(final FilterConfig filterConfig);
}
