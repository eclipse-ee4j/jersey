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

package org.glassfish.jersey.server.internal.process;

import java.util.function.Supplier;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import javax.inject.Inject;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.AsyncContext;
import org.glassfish.jersey.server.CloseableService;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;
import org.glassfish.jersey.server.spi.ExternalRequestScope;

/**
 * Configurator which initializes and register {@link ExternalRequestScope} instance into {@link InjectionManager}.
 *
 * @author Petr Bouda
 */
public class RequestProcessingConfigurator implements BootstrapConfigurator {

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        injectionManager.register(new ServerProcessingBinder());
    }

    private static class ContainerRequestFactory implements Supplier<ContainerRequest> {

        private final RequestProcessingContextReference reference;

        @Inject
        private ContainerRequestFactory(RequestProcessingContextReference reference) {
            this.reference = reference;
        }

        @Override
        public ContainerRequest get() {
            return reference.get().request();
        }
    }

    private static class UriRoutingContextFactory implements Supplier<UriRoutingContext> {

        private final RequestProcessingContextReference reference;

        @Inject
        private UriRoutingContextFactory(RequestProcessingContextReference reference) {
            this.reference = reference;
        }

        @Override
        public UriRoutingContext get() {
            return reference.get().uriRoutingContext();
        }
    }

    private static class CloseableServiceFactory implements Supplier<CloseableService> {

        private final RequestProcessingContextReference reference;

        @Inject
        private CloseableServiceFactory(RequestProcessingContextReference reference) {
            this.reference = reference;
        }

        @Override
        public CloseableService get() {
            return reference.get().closeableService();
        }
    }

    private static class AsyncContextFactory implements Supplier<AsyncContext> {

        private final RequestProcessingContextReference reference;

        @Inject
        private AsyncContextFactory(RequestProcessingContextReference reference) {
            this.reference = reference;
        }

        @Override
        public AsyncContext get() {
            return reference.get().asyncContext();
        }
    }

    /**
     * Defines server-side request processing injection bindings.
     *
     * @author Marek Potociar (marek.potociar at oracle.com)
     */
    private class ServerProcessingBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bindAsContract(RequestProcessingContextReference.class)
                    .in(RequestScoped.class);

            // Bind non-proxiable ContainerRequest injection injection points
            bindFactory(ContainerRequestFactory.class)
                    .to(ContainerRequest.class).to(ContainerRequestContext.class)
                    .proxy(false)
                    .in(RequestScoped.class);

            // Bind proxiable HttpHeaders, Request and ContainerRequestContext injection injection points
            bindFactory(ContainerRequestFactory.class)
                    .to(HttpHeaders.class).to(Request.class)
                    .proxy(true).proxyForSameScope(false)
                    .in(RequestScoped.class);

            // Bind proxiable UriInfo, ExtendedUriInfo and ResourceInfo injection points
            bindFactory(UriRoutingContextFactory.class)
                    .to(UriInfo.class).to(ExtendedUriInfo.class).to(ResourceInfo.class)
                    .proxy(true).proxyForSameScope(false)
                    .in(RequestScoped.class);

            // Bind proxiable SecurityContext injection point.
            // NOTE:
            // SecurityContext must be injected using the Injectee. The reason is that SecurityContext can be changed by filters,
            // but the proxy internally caches the first SecurityContext value injected in the RequestScope. This is prevented by
            // using SecurityContextInjectee that does not cache the SecurityContext instances and instead delegates calls to
            // the SecurityContext instance retrieved from current ContainerRequestContext.
            bind(SecurityContextInjectee.class)
                    .to(SecurityContext.class)
                    .proxy(true).proxyForSameScope(false)
                    .in(RequestScoped.class);

            // Bind proxiable CloseableService injection point.
            bindFactory(CloseableServiceFactory.class)
                    .to(CloseableService.class)
                    .proxy(true).proxyForSameScope(false)
                    .in(RequestScoped.class);

            // Bind proxiable AsyncContext and AsyncResponse injection points.
            // TODO maybe we can get rid of these completely? Or at least for AsyncContext?
            bindFactory(AsyncContextFactory.class)
                    .to(AsyncContext.class)
                    .to(AsyncResponse.class)
                    .in(RequestScoped.class);
        }
    }
}
