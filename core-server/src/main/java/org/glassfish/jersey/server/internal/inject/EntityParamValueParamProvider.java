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

import java.util.function.Function;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.Parameter;

/**
 * Provides injection of {@link Request} entity value or {@link Request} instance
 * itself.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Singleton
class EntityParamValueParamProvider extends AbstractValueParamProvider {

    /**
     * Creates new instance initialized with parameters.
     *
     * @param mpep Injected multivaluedParameterExtractor provider.
     */
    EntityParamValueParamProvider(Provider<MultivaluedParameterExtractorProvider> mpep) {
        super(mpep, Parameter.Source.ENTITY);
    }

    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        return new EntityValueSupplier(parameter);
    }

    private static class EntityValueSupplier implements Function<ContainerRequest, Object> {

        private final Parameter parameter;

        public EntityValueSupplier(Parameter parameter) {
            this.parameter = parameter;
        }

        @Override
        public Object apply(ContainerRequest containerRequest) {
            final Class<?> rawType = parameter.getRawType();

            Object value;
            if ((Request.class.isAssignableFrom(rawType) || ContainerRequestContext.class.isAssignableFrom(rawType))
                    && rawType.isInstance(containerRequest)) {
                value = containerRequest;
            } else {
                value = containerRequest.readEntity(rawType, parameter.getType(), parameter.getAnnotations());
                if (rawType.isPrimitive() && value == null) {
                    throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                            .entity(LocalizationMessages.ERROR_PRIMITIVE_TYPE_NULL()).build());
                }
            }
            return value;

        }
    }
}
