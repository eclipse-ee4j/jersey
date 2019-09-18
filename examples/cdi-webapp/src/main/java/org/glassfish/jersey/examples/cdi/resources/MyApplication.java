/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.cdi.resources;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application.
 *
 * @author Jonathan Benoit
 */
@ApplicationPath("/*")
public class MyApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();
        classes.add(MySingletonResource.class);
        classes.add(MyOtherResource.class);
        classes.add(HelloWorldResource.class);
        classes.add(EchoParamResource.class);
        classes.add(EchoParamFieldResource.class);
        classes.add(EchoParamConstructorResource.class);
        classes.add(ProxyInjectedAppScopedResource.class);
        classes.add(RequestScopedResource.class);
        return classes;
    }
}
