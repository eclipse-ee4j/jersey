/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.singleton;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class SingletonTestApp extends Application {

    static final SingletonResource[] SINGLETON_RESOURCES = new SingletonResource[3];

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();
        classes.add(SingletonResource.class);
        classes.add(SingletonTestContainerRequestFilter.class);
        classes.add(SingletonTestContainerResponseFilter.class);
        return classes;
    }
}
