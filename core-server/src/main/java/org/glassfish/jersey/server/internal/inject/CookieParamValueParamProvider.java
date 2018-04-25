/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ParamException;
import org.glassfish.jersey.server.model.Parameter;

/**
 * Value factory provider supporting the {@link CookieParam} injection annotation.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
final class CookieParamValueParamProvider extends AbstractValueParamProvider {

    private static final class CookieParamValueProvider implements Function<ContainerRequest, Object> {

        private final MultivaluedParameterExtractor<?> extractor;

        CookieParamValueProvider(MultivaluedParameterExtractor<?> extractor) {
            this.extractor = extractor;
        }

        @Override
        public Object apply(ContainerRequest containerRequest) {
            // TODO: cache?
            MultivaluedMap<String, String> cookies = new MultivaluedStringMap();

            for (Map.Entry<String, Cookie> e : containerRequest.getCookies().entrySet()) {
                cookies.putSingle(e.getKey(), e.getValue().getValue());
            }

            try {
                return extractor.extract(cookies);
            } catch (ExtractorException ex) {
                throw new ParamException.CookieParamException(ex.getCause(),
                        extractor.getName(), extractor.getDefaultValueString());
            }
        }
    }

    private static final class CookieTypeParamValueProvider  implements Function<ContainerRequest, Cookie> {

        private final String name;

        CookieTypeParamValueProvider(String name) {
            this.name = name;
        }

        @Override
        public Cookie apply(ContainerRequest containerRequest) {
            return containerRequest.getCookies().get(name);
        }
    }

    /**
     * {@link CookieParam} annotation value factory provider injection constructor.
     *
     * @param mpep            multivalued parameter extractor provider.
     */
    public CookieParamValueParamProvider(Provider<MultivaluedParameterExtractorProvider> mpep) {
        super(mpep, Parameter.Source.COOKIE);
    }

    @Override
    public Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        String parameterName = parameter.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid cookie parameter name
            return null;
        }

        if (parameter.getRawType() == Cookie.class) {
            return new CookieTypeParamValueProvider(parameterName);
        } else {
            MultivaluedParameterExtractor e = get(parameter);
            if (e == null) {
                return null;
            }
            return new CookieParamValueProvider(e);
        }
    }
}
