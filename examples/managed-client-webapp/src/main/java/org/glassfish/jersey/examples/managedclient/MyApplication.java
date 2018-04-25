/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedclient;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Jersey managed client example application.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class MyApplication extends ResourceConfig {

    /**
     * Create JAX-RS application for the example.
     */
    public MyApplication() {
        super(PublicResource.class, InternalResource.class, CustomHeaderFeature.class);
    }

    public static class MyClientAConfig extends ClientConfig {
        public MyClientAConfig() {
            this.register(new CustomHeaderFilter("custom-header", "a"));
        }
    }

    public static class MyClientBConfig extends ClientConfig {
        public MyClientBConfig() {
            this.register(new CustomHeaderFilter("custom-header", "b"));
        }
    }
}
