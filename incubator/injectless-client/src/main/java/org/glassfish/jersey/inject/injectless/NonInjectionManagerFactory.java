/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.inject.injectless;

import org.glassfish.jersey.client.innate.inject.NonInjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.core.Context;
import javax.ws.rs.RuntimeType;

/**
 * <p>
 *     This {@link InjectionManagerFactory} implementation provides a special {@link InjectionManager}. The highest priority
 *     of this injection manager is not to require any DI container. It is designed for pure REST client performing a request
 *     without a further requirements for performing injections in the customer client classes, such a filter or a provider.
 *     It means the customer classes do not have any injection points defined by {@link Inject} or {@link Context}.
 * </p>
 * <p>
 *     Using this injection manager does not prevent using any Jersey modules (such as Jersey-Media-Jackson module) from working
 *     with the client.
 * </p>
 */
@Priority(15)
@ConstrainedTo(RuntimeType.CLIENT)
public class NonInjectionManagerFactory implements InjectionManagerFactory {
    @Override
    public InjectionManager create(Object parent) {
        return new NonInjectionManager(false);
    }
}
