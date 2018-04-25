/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.sseitemstore.jersey;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * SSE item store JAX-RS application class.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@ApplicationPath("resources")
public class ItemStoreApp extends ResourceConfig {
    /**
     * Create new SSE Item Store Example JAX-RS application.
     */
    public ItemStoreApp() {
        super(ItemStoreResource.class, SseFeature.class);
    }
}
