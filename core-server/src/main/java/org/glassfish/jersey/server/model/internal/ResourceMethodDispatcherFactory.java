/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;

import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher;

/**
 * A resource method dispatcher provider factory.
 * <p>
 * This class is used by {@link org.glassfish.jersey.server.model.ResourceMethodInvoker} to
 * create a {@link org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher.Provider} instance
 * that will be used to provide a {@link org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher}
 * instances for all resource method invocations.
 * </p>
 * <p>
 * JAX-RS resource methods are invoked by {@link org.glassfish.jersey.server.model.ResourceMethodInvoker
 * resource method invoker}. Whenever a new incoming request has been routed to a resource method and the
 * method is being invoked by the resource method invoker, the invoker consults a registered
 * {@link org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher.Provider} instance to retrieve
 * a {@link org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher} that will be ultimately used
 * to dispatch the resource method invocation. This mechanism is useful for implementing any resource method
 * interception logic.
 * </p>
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
public final class ResourceMethodDispatcherFactory implements ResourceMethodDispatcher.Provider {

    private static final Logger LOGGER = Logger.getLogger(ResourceMethodDispatcherFactory.class.getName());
    private final Collection<ResourceMethodDispatcher.Provider> providers;

    ResourceMethodDispatcherFactory(Collection<ResourceMethodDispatcher.Provider> providers) {
        this.providers = providers;
    }

    // ResourceMethodDispatchProvider
    @Override
    public ResourceMethodDispatcher create(Invocable resourceMethod, InvocationHandler handler, ConfiguredValidator validator) {
        for (ResourceMethodDispatcher.Provider provider : providers) {
            try {
                ResourceMethodDispatcher dispatcher = provider.create(resourceMethod, handler, validator);
                if (dispatcher != null) {
                    return dispatcher;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        LocalizationMessages.ERROR_PROCESSING_METHOD(resourceMethod, provider.getClass().getName()),
                        e);
            }
        }

        return null;
    }
}
