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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.PathSegment;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ParamException.PathParamException;
import org.glassfish.jersey.server.model.Parameter;

/**
 * {@link PathParam &#64;PathParam} injection value provider.
 *
 * @author Paul Sandoz
 */
@Singleton
final class PathParamValueParamProvider extends AbstractValueParamProvider {

    /**
     * Injection constructor.
     *
     * @param mpep multivalued map parameter extractor provider.
     */
    public PathParamValueParamProvider(Provider<MultivaluedParameterExtractorProvider> mpep) {
        super(mpep, Parameter.Source.PATH);
    }

    @Override
    public Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        String parameterName = parameter.getSourceName();
        if (parameterName == null || parameterName.length() == 0) {
            // Invalid URI parameter name
            return null;
        }

        final Class<?> rawParameterType = parameter.getRawType();
        if (rawParameterType == PathSegment.class) {
            return new PathParamPathSegmentValueSupplier(parameterName, !parameter.isEncoded());
        } else if (rawParameterType == List.class && parameter.getType() instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) parameter.getType();
            Type[] targs = pt.getActualTypeArguments();
            if (targs.length == 1 && targs[0] == PathSegment.class) {
                return new PathParamListPathSegmentValueSupplier(parameterName, !parameter.isEncoded());
            }
        }

        MultivaluedParameterExtractor<?> e = get(parameter);
        if (e == null) {
            return null;
        }

        return new PathParamValueProvider(e, !parameter.isEncoded());
    }

    private static final class PathParamValueProvider implements Function<ContainerRequest, Object> {

        private final MultivaluedParameterExtractor<?> extractor;
        private final boolean decode;

        PathParamValueProvider(MultivaluedParameterExtractor<?> extractor, boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }

        @Override
        public Object apply(ContainerRequest request) {
            try {
                return extractor.extract(request.getUriInfo().getPathParameters(decode));
            } catch (ExtractorException e) {
                throw new PathParamException(e.getCause(), extractor.getName(), extractor.getDefaultValueString());
            }
        }
    }

    private static final class PathParamPathSegmentValueSupplier implements Function<ContainerRequest, PathSegment> {

        private final String name;
        private final boolean decode;

        PathParamPathSegmentValueSupplier(String name, boolean decode) {
            this.name = name;
            this.decode = decode;
        }

        @Override
        public PathSegment apply(ContainerRequest request) {
            List<PathSegment> ps = request.getUriInfo().getPathSegments(name, decode);
            if (ps.isEmpty()) {
                return null;
            }
            return ps.get(ps.size() - 1);
        }
    }

    private static final class PathParamListPathSegmentValueSupplier implements Function<ContainerRequest, List<PathSegment>> {

        private final String name;
        private final boolean decode;

        PathParamListPathSegmentValueSupplier(String name, boolean decode) {
            this.name = name;
            this.decode = decode;
        }

        @Override
        public List<PathSegment> apply(ContainerRequest request) {
            return request.getUriInfo().getPathSegments(name, decode);
        }
    }
}
