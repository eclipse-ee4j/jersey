/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.model.internal;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.InjectionManager;

/**
 * Invokes {@link PreDestroy} methods on all registered objects, when the injection manager is shut down.
 * <p/>
 * Some objects managed by Jersey are created using {@link InjectionManager#createAndInitialize}. This means
 * that such objects are created, dependencies injected and methods annotated with {@link javax.annotation.PostConstruct}
 * invoked. Therefore methods annotated with {@link PreDestroy} should be invoked on such objects too, when they are destroyed.
 * <p/>
 * This service invokes {@link PreDestroy} on all registered objects when {@link InjectionManager#shutdown()} is invoked
 * on the injection manager where this service is registered. Therefore only classes with their lifecycle linked
 * to the injection manager that created them should be registered here.
 *
 * @author Petr Janouch (petr.janouch at oracle.com)
 */
@Singleton
public class ManagedObjectsFinalizer {

    private final InjectionManager injectionManager;

    private final Set<Object> managedObjects = new HashSet<>();

    /**
     * Creates a new instance of {@link ManagedObjectsFinalizer}.
     *
     * @param injectionManager injection manager call {@code preDestroy} on managed objects.
     */
    public ManagedObjectsFinalizer(final InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
    }

    /**
     * Register an object for invocation of its {@link PreDestroy} method.
     * It will be invoked when the injection manager is shut down.
     *
     * @param object an object to be registered.
     */
    public void registerForPreDestroyCall(Object object) {
        managedObjects.add(object);
    }

    @PreDestroy
    public void preDestroy() {
        try {
            for (Object o : managedObjects) {
                injectionManager.preDestroy(o);
            }

        } finally {
            managedObjects.clear();
        }
    }
}
