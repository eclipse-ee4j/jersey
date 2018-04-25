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

package org.glassfish.jersey.server.internal.scanning;

import java.net.URI;
import java.util.Set;

import org.glassfish.jersey.server.ResourceFinder;

/**
 * An interface for scanning URI-based resources and reporting those resources
 * to a scanning listener.
 *
 * @author Paul Sandoz
 */
interface UriSchemeResourceFinderFactory {

    /**
     * Get the set of supported URI schemes.
     *
     * @return the supported URI schemes.
     */
    Set<String> getSchemes();

    /**
     * Create new {@link ResourceFinder} for a given resource URI.
     *
     * @param uri       resource URI.
     * @param recursive defines whether a resource finder should recursively scan any recognized sub-resource
     *                  URIs (value of {@code true}) or not (value of {@code false}).
     * @return resource finder for a given URI.
     */
    ResourceFinder create(URI uri, boolean recursive);
}

