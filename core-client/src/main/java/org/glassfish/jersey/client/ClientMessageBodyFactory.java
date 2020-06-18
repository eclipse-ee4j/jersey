/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InstanceBinding;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.MessageBodyFactory;

import javax.ws.rs.core.Configuration;

class ClientMessageBodyFactory extends MessageBodyFactory {

    /**
     * Keep reference to {@link ClientRuntime} so that {@code finalize} on it is not called
     * before the {@link MessageBodyFactory} is used.
     * <p>
     * Some entity types {@code @Inject} {@code MessageBodyFactory} for their {@code read} methods,
     * but if the finalizer is invoked before that, the HK2 injection manager gets closed.
     * </p>
     */
    private final LazyValue<ClientRuntime> clientRuntime;

    /**
     * Create a new message body factory.
     *
     * @param configuration configuration. Optional - can be null.
     * @param clientRuntimeValue - a reference to ClientRuntime.
     */
    private ClientMessageBodyFactory(Configuration configuration, Value<ClientRuntime> clientRuntimeValue) {
        super(configuration);
        clientRuntime = Values.lazy(clientRuntimeValue);
    }

    /**
     * Configurator which initializes and register {@link MessageBodyWorkers} instance into {@link InjectionManager} and
     * {@link BootstrapBag}.
     */
    static class MessageBodyWorkersConfigurator implements BootstrapConfigurator {

        private ClientMessageBodyFactory messageBodyFactory;
        private ClientRuntime clientRuntime;

        @Override
        public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
            messageBodyFactory = new ClientMessageBodyFactory(bootstrapBag.getConfiguration(), () -> clientRuntime);
            InstanceBinding<ClientMessageBodyFactory> binding =
                    Bindings.service(messageBodyFactory)
                            .to(MessageBodyWorkers.class);
            injectionManager.register(binding);
        }

        @Override
        public void postInit(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
            messageBodyFactory.initialize(injectionManager);
            bootstrapBag.setMessageBodyWorkers(messageBodyFactory);
        }

        void setClientRuntime(ClientRuntime clientRuntime) {
            this.clientRuntime = clientRuntime;
        }
    }

    ClientRuntime getClientRuntime() {
        return clientRuntime.get();
    }
}
