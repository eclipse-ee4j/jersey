/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.micrometer.server;

import io.micrometer.common.KeyValue;
import io.micrometer.common.util.StringUtils;
import io.micrometer.core.instrument.binder.http.Outcome;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.monitoring.RequestEvent;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Factory methods for {@link KeyValue KeyValues} associated with a request-response
 * exchange that is handled by Jersey server.
 */
class JerseyKeyValues {

    private static final KeyValue URI_NOT_FOUND = JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.URI
        .withValue("NOT_FOUND");

    private static final KeyValue URI_REDIRECTION = JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.URI
        .withValue("REDIRECTION");

    private static final KeyValue URI_ROOT = JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.URI
        .withValue("root");

    private static final KeyValue URI_UNKNOWN = JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.URI
            .withValue("UNKNOWN");

    private static final KeyValue EXCEPTION_NONE = JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.EXCEPTION
        .withValue("None");

    private static final KeyValue STATUS_SERVER_ERROR = JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.STATUS
        .withValue("500");

    private static final KeyValue METHOD_UNKNOWN = JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.METHOD
        .withValue("UNKNOWN");

    private JerseyKeyValues() {
    }

    /**
     * Creates a {@code method} KeyValue based on the {@link ContainerRequest#getMethod()
     * method} of the given {@code request}.
     * @param request the container request
     * @return the method KeyValue whose value is a capitalized method (e.g. GET).
     */
    static KeyValue method(ContainerRequest request) {
        return (request != null)
                ? JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.METHOD.withValue(request.getMethod())
                : METHOD_UNKNOWN;
    }

    /**
     * Creates a {@code status} KeyValue based on the status of the given
     * {@code response}.
     * @param response the container response
     * @return the status KeyValue derived from the status of the response
     */
    static KeyValue status(ContainerResponse response) {
        /* In case there is no response we are dealing with an unmapped exception. */
        return (response != null) ? JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.STATUS
            .withValue(Integer.toString(response.getStatus())) : STATUS_SERVER_ERROR;
    }

    /**
     * Creates a {@code uri} KeyValue based on the URI of the given {@code event}. Uses
     * the {@link ExtendedUriInfo#getMatchedTemplates()} if available. {@code REDIRECTION}
     * for 3xx responses, {@code NOT_FOUND} for 404 responses.
     * @param event the request event
     * @return the uri KeyValue derived from the request event
     */
    static KeyValue uri(RequestEvent event) {
        int status = 0;
        if (event.getContainerResponse() != null) {
            status = event.getContainerResponse().getStatus();
        } else if (WebApplicationException.class.isInstance(event.getException())) {
            Response webAppResponse = ((WebApplicationException) event.getException()).getResponse();
            if (webAppResponse != null) {
                status = webAppResponse.getStatus();
            }
        }
        if (status != 0) {
            if (JerseyTags.isRedirection(status) && event.getUriInfo().getMatchedResourceMethod() == null) {
                return URI_REDIRECTION;
            }
            if (status == 404 && event.getUriInfo().getMatchedResourceMethod() == null) {
                return URI_NOT_FOUND;
            }
            if (status >= 500 && status <= 599) {
                return STATUS_SERVER_ERROR;
            }
        }
        String matchingPattern = JerseyTags.getMatchingPattern(event);
        if (matchingPattern == null) {
            return URI_UNKNOWN;
        }
        if (matchingPattern.equals("/")) {
            return URI_ROOT;
        }
        return JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.URI.withValue(matchingPattern);
    }

    /**
     * Creates an {@code exception} KeyValue based on the {@link Class#getSimpleName()
     * simple name} of the class of the given {@code exception}.
     * @param event the request event
     * @return the exception KeyValue derived from the exception
     */
    static KeyValue exception(RequestEvent event) {
        Throwable exception = event.getException();
        if (exception == null) {
            return EXCEPTION_NONE;
        }
        ContainerResponse response = event.getContainerResponse();
        if (response != null) {
            int status = response.getStatus();
            if (status == 404 || JerseyTags.isRedirection(status)) {
                return EXCEPTION_NONE;
            }
        }
        if (exception.getCause() != null) {
            exception = exception.getCause();
        }
        String simpleName = exception.getClass().getSimpleName();
        return JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.EXCEPTION
            .withValue(StringUtils.isNotEmpty(simpleName) ? simpleName : exception.getClass().getName());
    }

    /**
     * Creates an {@code outcome} KeyValue based on the status of the given
     * {@code response}.
     * @param response the container response
     * @return the outcome KeyValue derived from the status of the response
     */
    static KeyValue outcome(ContainerResponse response) {
        if (response != null) {
            Outcome outcome = Outcome.forStatus(response.getStatus());
            return JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.OUTCOME.withValue(outcome.name());
        }
        /* In case there is no response we are dealing with an unmapped exception. */
        return JerseyObservationDocumentation.JerseyLegacyLowCardinalityTags.OUTCOME
            .withValue(Outcome.SERVER_ERROR.name());
    }

}
