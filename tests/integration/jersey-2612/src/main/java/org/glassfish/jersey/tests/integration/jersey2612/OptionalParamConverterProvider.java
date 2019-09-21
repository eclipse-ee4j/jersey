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

package org.glassfish.jersey.tests.integration.jersey2612;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;

import com.google.common.base.Function;
import com.google.common.base.Optional;

@Singleton
public class OptionalParamConverterProvider implements ParamConverterProvider {

    private final InjectionManager injectionManager;

    @Inject
    public OptionalParamConverterProvider(final InjectionManager injectionManager) {
        this.injectionManager = injectionManager;
    }

    @Override
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType, final Annotation[] annotations) {
        final List<ClassTypePair> ctps = ReflectionHelper.getTypeArgumentAndClass(genericType);
        final ClassTypePair ctp = (ctps.size() == 1) ? ctps.get(0) : null;
        if (ctp == null || ctp.rawClass() == String.class) {
            return new ParamConverter<T>() {
                @Override
                public T fromString(final String value) {
                    return rawType.cast(Optional.fromNullable(value));
                }

                @Override
                public String toString(final T value) throws IllegalArgumentException {
                    return value.toString();
                }
            };
        }
        final Set<ParamConverterProvider> converterProviders =
                Providers.getProviders(injectionManager, ParamConverterProvider.class);
        for (ParamConverterProvider provider : converterProviders) {
            @SuppressWarnings("unchecked")
            final ParamConverter<?> converter = provider.getConverter(ctp.rawClass(), ctp.type(), annotations);
            if (converter != null) {
                return new ParamConverter<T>() {
                    @Override
                    public T fromString(final String value) {
                        return rawType.cast(Optional.fromNullable(value)
                                                    .transform((Function<String, Object>) s -> converter.fromString(value)));
                    }

                    @Override
                    public String toString(final T value) throws IllegalArgumentException {
                        return value.toString();
                    }
                };
            }
        }
        return null;
    }
}
