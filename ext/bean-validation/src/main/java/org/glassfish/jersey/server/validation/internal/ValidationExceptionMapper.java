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

package org.glassfish.jersey.server.validation.internal;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;

import javax.inject.Provider;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationError;

/**
 * {@link ExceptionMapper} for {@link ValidationException}.
 * <p/>
 * If {@value ServerProperties#BV_SEND_ERROR_IN_RESPONSE} property is enabled then a list of {@link ValidationError}
 * instances is sent in {@link Response} as well (in addition to HTTP 400/500 status code). Supported media types are:
 * {@code application/json}/{@code application/xml} (in appropriate provider is registered on server) or
 * {@code text/html}/{@code text/plain} (via custom {@link ValidationErrorMessageBodyWriter}).
 *
 * @author Michal Gajdos
 */
public final class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOGGER = Logger.getLogger(ValidationExceptionMapper.class.getName());

    @Context
    private Configuration config;
    @Context
    private Provider<Request> request;

    @Override
    public Response toResponse(final ValidationException exception) {
        if (exception instanceof ConstraintViolationException) {
            LOGGER.log(Level.FINER, LocalizationMessages.CONSTRAINT_VIOLATIONS_ENCOUNTERED(), exception);

            final ConstraintViolationException cve = (ConstraintViolationException) exception;
            final Response.ResponseBuilder response = Response.status(ValidationHelper.getResponseStatus(cve));

            // Entity.
            final Object property = config.getProperty(ServerProperties.BV_SEND_ERROR_IN_RESPONSE);
            if (property != null && Boolean.valueOf(property.toString())) {
                final List<Variant> variants = Variant.mediaTypes(
                        MediaType.TEXT_PLAIN_TYPE,
                        MediaType.TEXT_HTML_TYPE,
                        MediaType.APPLICATION_XML_TYPE,
                        MediaType.APPLICATION_JSON_TYPE).build();
                final Variant variant = request.get().selectVariant(variants);
                if (variant != null) {
                    response.type(variant.getMediaType());
                } else {

                    // default media type which will be used only when none media type from {@value variants} is in accept
                    // header of original request.
                    // could be settable by configuration property.
                    response.type(MediaType.TEXT_PLAIN_TYPE);
                }
                response.entity(
                        new GenericEntity<>(
                                ValidationHelper.constraintViolationToValidationErrors(cve),
                                new GenericType<List<ValidationError>>() {}.getType()
                        )
                );
            }

            return response.build();
        } else {
            LOGGER.log(Level.WARNING, LocalizationMessages.VALIDATION_EXCEPTION_RAISED(), exception);

            return Response.serverError().entity(exception.getMessage()).build();
        }
    }
}
