/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

import org.glassfish.jersey.InjectionManagerProvider;
import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerSupplier;

/**
 * Extension of {@link InjectionManagerProvider} which contains helper static methods
 * that extract {@link InjectionManager} from client specific JAX-RS components.
 * <p>
 * See javadoc of {@link InjectionManagerProvider} for more details.
 * </p>
 *
 * @see InjectionManagerProvider
 * @author Miroslav Fuksa
 * @since 2.6
 */
public class InjectionManagerClientProvider extends InjectionManagerProvider {

    /**
     * Extract and return injection manager from {@link javax.ws.rs.client.ClientRequestContext clientRequestContext}.
     * The method can be used to inject custom types into a {@link javax.ws.rs.client.ClientRequestFilter}.
     *
     * @param clientRequestContext Client request context.
     *
     * @return injection manager.
     *
     * @throws java.lang.IllegalArgumentException when {@code clientRequestContext} is not a default
     * Jersey implementation provided by Jersey as argument in the
     * {@link javax.ws.rs.client.ClientRequestFilter#filter(javax.ws.rs.client.ClientRequestContext)} method.
     */
    public static InjectionManager getInjectionManager(ClientRequestContext clientRequestContext) {
        if (!(clientRequestContext instanceof InjectionManagerSupplier)) {
            throw new IllegalArgumentException(
                    LocalizationMessages
                            .ERROR_SERVICE_LOCATOR_PROVIDER_INSTANCE_REQUEST(clientRequestContext.getClass().getName()));
        }
        return ((InjectionManagerSupplier) clientRequestContext).getInjectionManager();
    }

    /**
     * Extract and return injection manager from {@link javax.ws.rs.client.ClientResponseContext clientResponseContext}.
     * The method can be used to inject custom types into a {@link javax.ws.rs.client.ClientResponseFilter}.
     *
     * @param clientResponseContext Client response context.
     *
     * @return injection manager.
     *
     * @throws java.lang.IllegalArgumentException when {@code clientResponseContext} is not a default
     * Jersey implementation provided by Jersey as argument in the
     * {@link javax.ws.rs.client.ClientResponseFilter#filter(javax.ws.rs.client.ClientRequestContext, javax.ws.rs.client.ClientResponseContext)}
     * method.
     */
    public static InjectionManager getInjectionManager(ClientResponseContext clientResponseContext) {
        if (!(clientResponseContext instanceof InjectionManagerSupplier)) {
            throw new IllegalArgumentException(
                    LocalizationMessages
                            .ERROR_SERVICE_LOCATOR_PROVIDER_INSTANCE_RESPONSE(clientResponseContext.getClass().getName()));
        }
        return ((InjectionManagerSupplier) clientResponseContext).getInjectionManager();
    }

}

