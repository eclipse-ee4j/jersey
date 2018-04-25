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

package org.glassfish.jersey.client;

import java.util.Map;
import java.util.function.Supplier;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.ext.MessageBodyReader;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.process.internal.RequestScoped;

/**
 * Registers all binders necessary for {@link Client} runtime.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
class ClientBinder extends AbstractBinder {

    private final Map<String, Object> clientRuntimeProperties;

    private static class RequestContextInjectionFactory extends ReferencingFactory<ClientRequest> {

        @Inject
        public RequestContextInjectionFactory(Provider<Ref<ClientRequest>> referenceFactory) {
            super(referenceFactory);
        }
    }

    private static class PropertiesDelegateFactory implements Supplier<PropertiesDelegate> {

        private final Provider<ClientRequest> requestProvider;

        @Inject
        private PropertiesDelegateFactory(Provider<ClientRequest> requestProvider) {
            this.requestProvider = requestProvider;
        }

        @Override
        public PropertiesDelegate get() {
            return requestProvider.get().getPropertiesDelegate();
        }
    }

    /**
     * Create new client binder for a new client runtime instance.
     *
     * @param clientRuntimeProperties map of client runtime properties.
     */
    ClientBinder(Map<String, Object> clientRuntimeProperties) {
        this.clientRuntimeProperties = clientRuntimeProperties;
    }

    @Override
    protected void configure() {
        install(new MessagingBinders.MessageBodyProviders(clientRuntimeProperties, RuntimeType.CLIENT),
                new MessagingBinders.HeaderDelegateProviders());

        bindFactory(ReferencingFactory.referenceFactory()).to(new GenericType<Ref<ClientConfig>>() {
        }).in(RequestScoped.class);

        bindFactory(RequestContextInjectionFactory.class)
                .to(ClientRequest.class)
                .in(RequestScoped.class);

        bindFactory(ReferencingFactory.referenceFactory()).to(new GenericType<Ref<ClientRequest>>() {
        }).in(RequestScoped.class);

        bindFactory(PropertiesDelegateFactory.class, Singleton.class).to(PropertiesDelegate.class).in(RequestScoped.class);

        // ChunkedInput entity support
        bind(ChunkedInputReader.class).to(MessageBodyReader.class).in(Singleton.class);
    }
}
