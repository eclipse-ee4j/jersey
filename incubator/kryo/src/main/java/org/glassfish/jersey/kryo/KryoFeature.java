/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.kryo;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.Beta;
import org.glassfish.jersey.kryo.internal.KryoMessageBodyProvider;
import org.glassfish.jersey.kryo.internal.RegistrationNotRequiredKryoContextResolver;

/**
 * <p>
 * Feature used to register Kryo providers.
 * </p>
 * <p>
 * For the security reasons, Kryo#setRegistrationRequired(true) should be specified.
 * Unless {@code KryoFeature#registrationRequired(false)} is registered, a {@code ContextResolver<Kryo>} should be registered.
 * There the user is expected to create new {@code Kryo} instance with the registrations:
 * <pre>
 * public Kryo getContext(Class<?> type) {
 *      ...
 *      Kryo kryo = new Kryo();
 *      kryo.setRegistrationRequired(true);
 *      kryo.register(The_class_for_which_the_KryoMessageBodyProvider_should_be_allowed);
 *      ...
 *      return kryo;
 * }
 * </pre>
 * Note that {@code ContextResolver#getContext} is invoked just once when creating {@code KryoPool} and the {@code type} argument
 * is {@code null}.
 * </p>
 *
 * @author Libor Kramolis
 */
@Beta
public class KryoFeature implements Feature {

    private final boolean registrationRequired;

    public KryoFeature() {
        registrationRequired = true;
    }

    public static KryoFeature registrationRequired(boolean registrationRequired) {
        return new KryoFeature(registrationRequired);
    }

    private KryoFeature(boolean registrationRequired) {
        this.registrationRequired = registrationRequired;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        if (!config.isRegistered(KryoMessageBodyProvider.class)) {
            context.register(KryoMessageBodyProvider.class);
            if (!registrationRequired) {
                context.register(RegistrationNotRequiredKryoContextResolver.class);
            }
        }

        return true;
    }

}
