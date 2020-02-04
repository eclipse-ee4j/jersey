/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.httppatch;

import java.util.logging.Logger;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Patchable resource.
 *
 * @author Gerard Davison (gerard.davison at oracle.com)
 * @author Marek Potociar
 */
@Path(App.ROOT_PATH)
@Produces(MediaType.APPLICATION_JSON)
public class PatchableResource {
    private static volatile State state = new State();
    private static final Logger LOGGER = Logger.getLogger(PatchableResource.class.getName());

    /**
     * Get current resource state.
     *
     * @return current resource state.
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public State getState() {
        return state;
    }

    /**
     * Set new resource state.
     * <p>
     * We only need to replace the resource state with the one passed into the method
     * as the actual state patching occurred in the {@link PatchingInterceptor}.
     * </p>
     *
     * @param newState new resource state.
     * @return patched state.
     */
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    public State patchState(State newState) {
        LOGGER.info("New resource state: " + newState.toString());
        state = newState;
        return newState;
    }
}
