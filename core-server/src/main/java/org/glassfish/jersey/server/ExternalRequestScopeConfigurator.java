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

package org.glassfish.jersey.server;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.internal.BootstrapBag;
import org.glassfish.jersey.internal.BootstrapConfigurator;
import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.spi.ComponentProvider;
import org.glassfish.jersey.server.spi.ExternalRequestContext;
import org.glassfish.jersey.server.spi.ExternalRequestScope;

/**
 * Configurator which initializes and register {@link ExternalRequestScope} instance into {@link InjectionManager}.
 *
 * @author Petr Bouda
 */
class ExternalRequestScopeConfigurator implements BootstrapConfigurator {

    private static final Logger LOGGER = Logger.getLogger(ExternalRequestScopeConfigurator.class.getName());

    @Override
    public void init(InjectionManager injectionManager, BootstrapBag bootstrapBag) {
        ServerBootstrapBag serverBag = (ServerBootstrapBag) bootstrapBag;

        Class<ExternalRequestScope>[] extScopes = ServiceFinder.find(ExternalRequestScope.class, true).toClassArray();
        boolean extScopeBound = false;

        if (extScopes.length == 1) {
            for (ComponentProvider p : serverBag.getComponentProviders().get()) {
                if (p.bind(extScopes[0], Collections.singleton(ExternalRequestScope.class))) {
                    extScopeBound = true;
                    break;
                }
            }
        } else if (extScopes.length > 1) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                StringBuilder scopeList = new StringBuilder("\n");
                for (Class<ExternalRequestScope> ers : extScopes) {
                    scopeList.append("   ").append(ers.getTypeParameters()[0]).append('\n');
                }
                LOGGER.warning(LocalizationMessages.WARNING_TOO_MANY_EXTERNAL_REQ_SCOPES(scopeList.toString()));
            }
        }

        if (!extScopeBound) {
            injectionManager.register(new NoopExternalRequestScopeBinder());
        }
    }

    private static class NoopExternalRequestScopeBinder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(NOOP_EXTERNAL_REQ_SCOPE).to(ExternalRequestScope.class);
        }
    }

    private static final ExternalRequestScope<Object> NOOP_EXTERNAL_REQ_SCOPE = new ExternalRequestScope<Object>() {

        @Override
        public ExternalRequestContext<Object> open(InjectionManager injectionManager) {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public void suspend(ExternalRequestContext<Object> o, InjectionManager injectionManager) {
        }

        @Override
        public void resume(ExternalRequestContext<Object> o, InjectionManager injectionManager) {
        }
    };
}
