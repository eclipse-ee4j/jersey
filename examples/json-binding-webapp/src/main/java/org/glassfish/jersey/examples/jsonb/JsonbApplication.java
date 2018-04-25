/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jsonb;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Jersey JSON-B example application.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@ApplicationPath("/")
public class JsonbApplication extends ResourceConfig {
    public JsonbApplication() {
        register(JsonbResource.class);
    }
}
