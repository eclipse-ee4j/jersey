/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.sseitemstore.jaxrs;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * SSE item store JAX-RS application class.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@ApplicationPath("resources")
public class JaxrsItemStoreApp extends ResourceConfig {
    /**
     * Create new SSE Item Store Example JAX-RS application.
     */
    public JaxrsItemStoreApp() {
        super(JaxrsItemStoreResource.class);
    }
}
