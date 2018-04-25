/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import org.glassfish.jersey.uri.PathPattern;

/**
 * Marker interface for all resource model components that contain path information
 * usable for routing.
 *
 * @author Marc Hadley
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public interface Routed {

    /**
     * Get the path direct assigned to the component.
     *
     * @return component path.
     */
    public String getPath();

    /**
     * Get the path pattern that can be used for matching the remaining
     * request URI against this component represented by this model.
     *
     * @return component path pattern.
     */
    public PathPattern getPathPattern();
}
