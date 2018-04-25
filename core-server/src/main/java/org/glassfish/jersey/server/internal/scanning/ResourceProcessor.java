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

import java.io.IOException;
import java.io.InputStream;

/**
 * Processes resources found by {@link org.glassfish.jersey.server.ResourceFinder}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public interface ResourceProcessor {

    /**
     * Accept a scanned resource.
     * <p>
     *
     * @param name the resource name.
     * @return true if the resource is accepted for processing, otherwise false.
     */
    boolean accept(String name);

    /**
     * Process a scanned resource.
     * <p>
     * This method will be invoked after the listener has accepted the
     * resource.
     *
     * @param name the resource name.
     * @param in the input stream of the resource
     * @throws java.io.IOException if an error occurs when processing the resource.
     */
    void process(String name, InputStream in) throws IOException;

}
