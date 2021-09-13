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

package org.glassfish.jersey.inject.weld.managed;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.ws.rs.RuntimeType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.inject.weld.internal.injector.JerseyClientCreationalContext;
import org.glassfish.jersey.inject.weld.internal.managed.CdiInjectionManagerFactoryBase;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerFactory;
import org.glassfish.jersey.server.ApplicationHandler;

/**
 * SPI implementation of {@link InjectionManagerFactory} which provides a new instance of CDI {@link InjectionManager}.
 */
@Priority(20)
public class CdiInjectionManagerFactory extends CdiInjectionManagerFactoryBase implements InjectionManagerFactory {

    @Override
    // TODO deprecate this in favor of #create(Object parent, RuntimeType runtimeType)
    public InjectionManager create(Object parent) {
        return create(parent, getRuntimeType());
    }

    /**
     * Create injectionManager for {@link RuntimeType#CLIENT or get the existing injection manager for the server}
     * @param parent Parent injection manager. Not used in this InjectionManagerFactory.
     * @param runtimeType {@link RuntimeType} to get or create the proper injection manager.
     * @return The required injection manager instance.
     */
    public InjectionManager create(Object parent, RuntimeType runtimeType) {
        return getInjectionManager(runtimeType);
    }

    /**
     * Get the client side InjectionManager stored in the {@link CreationalContext} or the server side InjectionManager.
     * @param creationalContext {@link CreationalContext} subclass which may hold InjectionManager for the client
     * @return existing client side injection or server side injection manager.
     */
    public static InjectionManager getInjectionManager(CreationalContext<?> creationalContext) {
        if (JerseyClientCreationalContext.class.isInstance(creationalContext)) {
            return ((JerseyClientCreationalContext) creationalContext).getInjectionManager();
        } else {
            return getInjectionManager(RuntimeType.SERVER);
        }
    }

    // TODO refactor to call InjectionManagerFactory#create(Object, RuntimeType);
    private static RuntimeType getRuntimeType() {
        Exception e = new RuntimeException();
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().equals(ClientConfig.class.getName())) {
                return RuntimeType.CLIENT;
            }
            if (element.getClassName().equals(ApplicationHandler.class.getName())) {
                return RuntimeType.SERVER;
            }
        }
        return RuntimeType.SERVER;
    }
}
