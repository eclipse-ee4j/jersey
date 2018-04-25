/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.osgi.helloworld.resource;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class MyApplication extends Application {

    static final Set<Class<?>> classes = new HashSet<Class<?>>() {
        {
            add(HelloWorldResource.class);
            add(AnotherResource.class);
        }};

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
