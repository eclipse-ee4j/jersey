/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

import javax.inject.Singleton;

/**
 * An aggregate {@link ParamConverterProvider param converter provider} that loads all
 * the registered {@link ParamConverterProvider} implementations.
 * <p />
 * When invoked, the provider iterates through the registered implementations until
 * it finds the first implementation that returns a non-null {@link ParamConverter param converter},
 * which is subsequently returned from the factory. In case no non-null string reader
 * instance is found, {@code null} is returned from the factory. {@link org.glassfish.jersey.internal.inject.Custom Custom}
 * providers are iterated first, so that user registered providers are preferred against internal jersey providers.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Miroslav Fuksa
 */
@Singleton
public class ParamConverterFactory implements ParamConverterProvider {

    private final List<ParamConverterProvider> converterProviders;

    ParamConverterFactory(Set<ParamConverterProvider> providers, Set<ParamConverterProvider> customProviders) {

        Set<ParamConverterProvider> copyProviders = new LinkedHashSet<>(providers);
        converterProviders = new ArrayList<>();
        converterProviders.addAll(customProviders);
        copyProviders.removeAll(customProviders);
        converterProviders.addAll(copyProviders);
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        for (ParamConverterProvider provider : converterProviders) {
            @SuppressWarnings("unchecked")
            ParamConverter<T> converter = provider.getConverter(rawType, genericType, annotations);
            if (converter != null) {
                return converter;
            }
        }
        return null;
    }
}
