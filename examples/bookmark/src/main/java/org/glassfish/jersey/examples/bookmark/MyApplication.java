/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.examples.bookmark.resource.UsersResource;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Michal Gajdos
 */
@ApplicationPath("/")
public class MyApplication extends ResourceConfig {

    public MyApplication() {
        registerClasses(UsersResource.class);
        register(new JettisonFeature());
    }
}
