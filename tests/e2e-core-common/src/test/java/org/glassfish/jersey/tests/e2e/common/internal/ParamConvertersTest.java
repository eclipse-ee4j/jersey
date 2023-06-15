/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.inject.ParamConverterConfigurator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.util.Date;

public class ParamConvertersTest {
    private CommonConfig config;

    @BeforeEach
    public void setUp() throws Exception {
        config = new CommonConfig(null, ComponentBag.INCLUDE_ALL);
    }

    @Test
    public void paramConvertersReturnNull() {
        ParamConverter<String> converter = getParamConverter(String.class);
        assertNull(String.class);
        assertNull(Date.class);
        assertNull(Character.class);
    }

    @Test
    public void paramConvertersThrow() {
        config.property(CommonProperties.PARAM_CONVERTERS_THROW_IAE, true);

        assertThrow(String.class);
        assertThrow(Date.class);
        assertThrow(Character.class);
    }

    private <T> void assertNull(Class<T> clazz) {
        ParamConverter<T> converter = getParamConverter(clazz);
        Assertions.assertNotNull(converter);
        Assertions.assertNull(converter.fromString(null));
        Assertions.assertNull(converter.toString(null));
    }

    private <T> void assertThrow(Class<T> clazz) {
        ParamConverter<T> converter = getParamConverter(clazz);
        Assertions.assertNotNull(converter);

        try {
            converter.fromString(null);
            throw new RuntimeException("The IAE was not thrown");
        } catch (IllegalArgumentException illegalArgumentException) {
            //expected
        }

        try {
            converter.toString(null);
            throw new RuntimeException("The IAE was not thrown");
        } catch (IllegalArgumentException illegalArgumentException) {
            //expected
        }
    }

    private <T> ParamConverter<T> getParamConverter(Class<T> clazz) {
        InjectionManager injectionManager = Injections.createInjectionManager();
        new ParamConverterConfigurator().init(injectionManager, null);
        injectionManager.register(Bindings.service(config).to(Configuration.class));
        injectionManager.completeRegistration();

        final Iterable<ParamConverterProvider> allProviders =
                Providers.getAllProviders(injectionManager, ParamConverterProvider.class, new RankedComparator<>());
        for (ParamConverterProvider provider : allProviders) {
            ParamConverter<T> converter = provider.getConverter(clazz, clazz, null);
            if (converter != null) {
                return converter;
            }
        }
        return null;
    }

}
