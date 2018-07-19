/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey3796;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the purpose of the whole test.
 * Here we test if initialized providers (by interfaces) are initialized only once per scope.
 * This means the one class per all implemented provider interfaces.
 */
@Provider
@Priority(Priorities.USER + 2)
public class MyProvider implements Feature, ContainerRequestFilter, ContainerResponseFilter {

    /**
     * common map which in case of wrong initialization could cause NPE or not contain some expected element
     */
    private Map<String, String> sameInstance;

    /**
     * Feature method which is invoked as the first and initializes the common map.
     * It also puts the toString value of the implementing class into the map
     *
     * @param context feature context (here just skipped)
     * @return true - always success
     */
    @Override
    public boolean configure(FeatureContext context) {
        sameInstance = new HashMap<>();
        sameInstance.put("Feature", this.toString());
        return true;
    }

    /**
     * Request filter provider - uses the common map (if it's not initialized the NPE is thrown).
     * Puts the toString value of the implementing class into the map
     *
     * @param requestContext request context (here just skipped)
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        sameInstance.put("Request", this.toString());
    }

    /**
     * Response filter provider - uses the common map (and is called as the last in the providers sequence)
     * if map is not initialized the NPE is thrown.
     * Puts the toString value of the implementing class into the map
     *
     * maps all elements of the map to the response entity (which is map is well)
     *
     * @param requestContext    request context (here just skipped)
     * @param responseContext   response context (here just skipped)
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        sameInstance.put("Response", this.toString());
        ((Map) responseContext.getEntity()).putAll(sameInstance);
    }

}