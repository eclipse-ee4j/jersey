/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Response;

import javax.inject.Singleton;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.spi.internal.ResourceMethodDispatcher;

/**
 * Specific resource method dispatcher for dispatching requests to a void
 * {@link java.lang.reflect.Method Java method} with no input arguments
 * using a supplied {@link InvocationHandler Java method invocation handler}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Singleton
final class VoidVoidDispatcherProvider implements ResourceMethodDispatcher.Provider {

    private final ResourceContext resourceContext;

    VoidVoidDispatcherProvider(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    private static class VoidToVoidDispatcher extends AbstractJavaResourceMethodDispatcher {

        private VoidToVoidDispatcher(final Invocable resourceMethod,
                                     final InvocationHandler handler,
                                     final ConfiguredValidator validator) {
            super(resourceMethod, handler, validator);
        }

        @Override
        public Response doDispatch(final Object resource, final ContainerRequest containerRequest) throws ProcessingException {
            invoke(containerRequest, resource);
            return Response.noContent().build();
        }
    }

    @Override
    public ResourceMethodDispatcher create(final Invocable resourceMethod,
                                           final InvocationHandler handler,
                                           final ConfiguredValidator validator) {
        if (resourceMethod.getHandlingMethod().getReturnType() != void.class || !resourceMethod.getParameters().isEmpty()) {
            return null;
        }

        return resourceContext.initResource(new VoidToVoidDispatcher(resourceMethod, handler, validator));
    }
}
