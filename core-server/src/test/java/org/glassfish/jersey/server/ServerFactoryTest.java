/*
 * Copyright (c) 2018 Markus KARG. All rights reserved.
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.theInstance;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.JAXRS;
import javax.ws.rs.JAXRS.Configuration;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.ServiceFinder.ServiceIteratorProvider;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.Server;
import org.glassfish.jersey.server.spi.ServerProvider;
import org.junit.Test;

/**
 * Unit tests for {@link ServerFactory}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 * @since 2.28
 */
public final class ServerFactoryTest {

    @Test
    public final void shouldBuildServer() {
        // given
        final Application mockApplication = new Application();
        final JAXRS.Configuration mockConfiguration = name -> null;
        final Server mockServer = new Server() {

            @Override
            public final Container container() {
                return null;
            }

            @Override
            public final int port() {
                return 0;
            }

            @Override
            public final CompletionStage<?> start() {
                return null;
            }

            @Override
            public final CompletionStage<?> stop() {
                return null;
            }

            @Override
            public final <T> T unwrap(final Class<T> nativeClass) {
                return null;
            }
        };
        ServiceFinder.setIteratorProvider(new ServiceIteratorProvider() {

            @Override
            public final <T> Iterator<T> createIterator(final Class<T> service, final String serviceName,
                    final ClassLoader loader, final boolean ignoreOnClassNotFound) {
                return Collections.singleton(service.cast(new ServerProvider() {

                    @Override
                    public final <U extends Server> U createServer(final Class<U> type, final Application application,
                            final Configuration configuration) {
                        return application == mockApplication && configuration == mockConfiguration
                                ? type.cast(mockServer)
                                : null;
                    }
                })).iterator();
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

}
