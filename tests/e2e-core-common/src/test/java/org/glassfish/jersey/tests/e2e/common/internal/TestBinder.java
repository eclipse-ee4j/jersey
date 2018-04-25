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

package org.glassfish.jersey.tests.e2e.common.internal;

import java.util.Collections;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.ProviderBinder;
import org.glassfish.jersey.message.internal.MessagingBinders;

/**
 * Binder for testing purposes.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class TestBinder extends AbstractBinder {

    public static void initProviders(final InjectionManager injectionManager) {
        initProviders(injectionManager, Collections.emptySet(), Collections.emptySet());
    }

    public static void initProviders(final InjectionManager injectionManager,
                                     final Iterable<Class<?>> providerClasses,
                                     final Iterable<Object> providerInstances) {
        final ProviderBinder providerBinder = new ProviderBinder(injectionManager);
        providerBinder.bindClasses(providerClasses);
        providerBinder.bindInstances(providerInstances);
    }

    @Override
    protected void configure() {
        install(new MessagingBinders.MessageBodyProviders(null, RuntimeType.SERVER));

        bind(new ExceptionMapper<Throwable>() {
            @Override
            public Response toResponse(Throwable exception) {
                if (exception instanceof NumberFormatException) {
                    return Response.ok(-1).build();
                }

                throw new RuntimeException(exception);
            }
        }).to(ExceptionMapper.class);
    }
}
