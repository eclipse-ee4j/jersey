/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.jaxrs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application class for this example.
 *
 * @author Martin Matula
 */
public class JaxRsApplication extends Application {
    private final Set<Class<?>> classes;

    public JaxRsApplication() {
        HashSet<Class<?>> c = new HashSet<Class<?>>();
        c.add(HelloWorldResource.class);
        classes = Collections.unmodifiableSet(c);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
