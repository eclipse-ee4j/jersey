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

package org.glassfish.jersey.server.spi;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.ws.rs.JAXRS;
import javax.ws.rs.JAXRS.Configuration;
import javax.ws.rs.JAXRS.Configuration.Builder;

import org.glassfish.jersey.internal.ServiceFinder;
import org.glassfish.jersey.internal.ServiceFinder.ServiceIteratorProvider;
import org.glassfish.jersey.internal.guava.Iterators;
import org.junit.After;
import org.junit.Test;

/**
 * Unit tests for {@link ConfiguratorFactory}.
 *
 * @author Markus KARG (markus@headcrashing.eu)
 */
public final class ConfiguratorFactoryTest {

    @Test
    public final void shouldConfigureUsingAllProviders() {
        // given
        final Function<String, String> configuration = name -> name + "Value";
        final Map<String, String> properties = new HashMap<>(2);
        final JAXRS.Configuration.Builder configurationBuilder = new JAXRS.Configuration.Builder() {
            @Override
            public final JAXRS.Configuration.Builder property(final String name, final Object value) {
                properties.put(name, value.toString());
                return this;
            }

            @Override
            public final <T> Builder from(final BiFunction<String, Class<T>, Optional<T>> propertiesProvider) {
                return null;
            }

            @Override
            public final Configuration build() {
                return null;
            }
        };
        final Configurator firstConfigurator = new Configurator() {
            @SuppressWarnings("unchecked")
            @Override
            public final void configure(final JAXRS.Configuration.Builder configurationBuilder,
                    final Object configuration) {
                configurationBuilder.property("firstProperty",
                        ((Function<String, String>) configuration).apply("first"));
            }
        };
        final Configurator secondConfigurator = new Configurator() {
            @SuppressWarnings("unchecked")
            @Override
            public final void configure(final JAXRS.Configuration.Builder configurationBuilder,
                    final Object configuration) {
                configurationBuilder.property("secondProperty",
                        ((Function<String, String>) configuration).apply("second"));
            }
        };
        ServiceFinder.setIteratorProvider(new ServiceIteratorProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> Iterator<T> createIterator(final Class<T> service, final String serviceName,
                    final ClassLoader loader,
                    final boolean ignoreOnClassNotFound) {
                return service == Configurator.class ? Iterators.forArray(service.cast(firstConfigurator),
                        service.cast(secondConfigurator)) : Collections.emptyIterator();
            }

            @Override
            public final <T> Iterator<Class<T>> createClassIterator(final Class<T> service, final String serviceName,
                    final ClassLoader loader,
                    final boolean ignoreOnClassNotFound) {
                throw new UnsupportedOperationException();
            }
        });

        // when
        ConfiguratorFactory.configure(configurationBuilder, configuration);

        // then
        assertThat(properties,
                both(hasEntry("firstProperty", "firstValue")).and(hasEntry("secondProperty", "secondValue")));
    }

    @After
    public final void resetServiceFinder() {
        ServiceFinder.setIteratorProvider(null);
    }

}
