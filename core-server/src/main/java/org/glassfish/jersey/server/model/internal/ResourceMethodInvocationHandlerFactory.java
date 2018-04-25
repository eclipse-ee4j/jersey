/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model.internal;

import java.lang.reflect.InvocationHandler;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.internal.ResourceMethodInvocationHandlerProvider;

/**
 * An injectable {@link ResourceMethodInvocationHandlerProvider resource method
 * invocation handler provider} factory.
 * <p />
 * When invoked, the factory iterates over the registered custom {@link ResourceMethodInvocationHandlerProvider
 * resource method invocation handler providers} invoking their
 * {@link ResourceMethodInvocationHandlerProvider#create(org.glassfish.jersey.server.model.Invocable) createPatternFor(...)}
 * methods and returns the first non-null {@link InvocationHandler
 * invocation handler} instance retrieved from the providers. If no custom providers
 * are available, or if none of the providers returns a non-null invocation handler,
 * in such case a default invocation handler provided by the factory is returned.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
public final class ResourceMethodInvocationHandlerFactory implements ResourceMethodInvocationHandlerProvider {

    private static final InvocationHandler DEFAULT_HANDLER = (target, method, args) -> method.invoke(target, args);
    private static final Logger LOGGER = Logger.getLogger(ResourceMethodInvocationHandlerFactory.class.getName());
    private final LazyValue<Set<ResourceMethodInvocationHandlerProvider>> providers;

    ResourceMethodInvocationHandlerFactory(InjectionManager injectionManager) {
        this.providers = Values.lazy((Value<Set<ResourceMethodInvocationHandlerProvider>>)
                () -> Providers.getProviders(injectionManager, ResourceMethodInvocationHandlerProvider.class));
    }

    // ResourceMethodInvocationHandlerProvider
    @Override
    public InvocationHandler create(Invocable resourceMethod) {
        for (ResourceMethodInvocationHandlerProvider provider : providers.get()) {
            try {
                InvocationHandler handler = provider.create(resourceMethod);
                if (handler != null) {
                    return handler;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, LocalizationMessages.ERROR_PROCESSING_METHOD(
                        resourceMethod,
                        provider.getClass().getName()), e);
            }
        }

        return DEFAULT_HANDLER;
    }
}
