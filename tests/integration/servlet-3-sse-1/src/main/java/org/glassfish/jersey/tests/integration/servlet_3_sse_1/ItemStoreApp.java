/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_3_sse_1;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * SSE item store JAX-RS application class.
 *
 * @author Marek Potociar
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
