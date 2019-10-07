/*
 * Copyright (c) 2018, Markus KARG. All rights reserved.
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.jmockit.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.theInstance;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import jakarta.ws.rs.SeBootstrap;
import jakarta.ws.rs.SeBootstrap.Configuration;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.ServiceFinder.ServiceIteratorProvider;
import org.glassfish.jersey.internal.guava.Iterators;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.InjectionManagerFactory;
import org.glassfish.jersey.server.ServerFactory;
import org.glassfish.jersey.server.spi.Server;
import org.glassfish.jersey.server.spi.ServerProvider;
import org.junit.After;
import org.junit.Test;

import mockit.Mocked;

/**
 * Unit tests for {@link ServerFactory}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 3.0
 */
public final class ServerFactoryTest {

    @Test
    public final void shouldBuildServer(@Mocked final Application mockApplication, @Mocked final Server mockServer,
            @Mocked final SeBootstrap.Configuration mockConfiguration, @Mocked final InjectionManager mockInjectionManager) {
        // given
        ServiceFinder.setIteratorProvider(new ServiceIteratorProvider() {
            @Override
            public final <T> Iterator<T> createIterator(final Class<T> service, final String serviceName,
                    final ClassLoader loader, final boolean ignoreOnClassNotFound) {
                return Iterators.singletonIterator(service.cast(
                        service == ServerProvider.class ? new ServerProvider() {
                            @Override
                            public final <U extends Server> U createServer(final Class<U> type, final Application application,
                                    final Configuration configuration) {
                                return application == mockApplication && configuration == mockConfiguration
                                        ? type.cast(mockServer)
                                        : null;
                            }
                        }
                                : service == InjectionManagerFactory.class ? new InjectionManagerFactory() {
                                    @Override
                                    public final InjectionManager create(final Object parent) {
                                        return mockInjectionManager;
                                    }
                                }
                                        : null));
            }

            @Override
            public final <T> Iterator<Class<T>> createClassIterator(final Class<T> service, final String serviceName,
                    final ClassLoader loader, final boolean ignoreOnClassNotFound) {
                return null;
            }
        });

        // when
        final Server server = ServerFactory.createServer(Server.class, mockApplication, mockConfiguration);

        // then
        assertThat(server, is(theInstance(mockServer)));
    }

    @After
    public final void resetServiceFinder() {
        ServiceFinder.setIteratorProvider(null);
    }

}
