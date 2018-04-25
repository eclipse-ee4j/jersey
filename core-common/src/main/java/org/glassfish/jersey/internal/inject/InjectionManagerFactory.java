/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.inject;

/**
 * Factory which is able to create {@link InjectionManager}. Every DI provider must create its own {@link InjectionManagerFactory}
 * and register it in META-INF.services. Then the {@code InjectionManagerFactory} can be looked up and {@code InjectionManager}
 * can be created.
 */
public interface InjectionManagerFactory {

    /**
     * Load a new injection manager without parent and initial binder.
     *
     * @return initialized injection manager.
     */
    default InjectionManager create() {
        return create(null);
    }

    /**
     * Load a new injection manager with parent object.
     *
     * @param parent injection manager parent or concrete DI specific object which is compatible with DI provider.
     * @return initialized injection manager.
     */
    InjectionManager create(Object parent);
}
