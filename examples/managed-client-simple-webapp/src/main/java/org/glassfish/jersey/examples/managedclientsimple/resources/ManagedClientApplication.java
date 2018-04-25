/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.managedclientsimple.resources;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * {@link ResourceConfig resource config} registering resource for managed client simple sample.
 *
 * @author Miroslav Fuksa
 */
@ApplicationPath("/app")
public class ManagedClientApplication extends ResourceConfig {

    public ManagedClientApplication() {
        super(ClientResource.class, StandardResource.class);
    }

}
