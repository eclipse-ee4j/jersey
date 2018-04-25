/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.servlet3.webapp;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Test Application subclass for servlet3-webapp example.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class App extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(DogResource.class);
        classes.add(CatResource.class);

        return classes;
    }
}
