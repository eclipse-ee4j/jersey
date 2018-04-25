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

package org.glassfish.jersey.tests.integration.multimodule.ejb.reload.lib;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.inject.Singleton;

/**
 * JAX-RS resource registered as a singleton
 * allows to detect when application got initiated as the value
 * returned from its getNano resource method
 * will get adjusted with each reload.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("last-init-nano-time")
public class ReloadDetectionResource {

    final long ns = System.nanoTime();

    private ReloadDetectionResource() {
        // prevent instantiation
    }

    /**
     * This is the only mean how to get a new instance of the resource.
     *
     * @return new reload detection resource.
     */
    public static final ReloadDetectionResource createNewInstance() {
        return new ReloadDetectionResource();
    }


    @GET
    public long getNano() {
        return ns;
    }
}
