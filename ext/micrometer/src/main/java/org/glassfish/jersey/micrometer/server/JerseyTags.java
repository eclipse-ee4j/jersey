/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

import java.util.List;
import java.util.regex.Pattern;

import io.micrometer.common.util.StringUtils;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.http.Outcome;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.uri.UriTemplate;

/**
 * Factory methods for {@link Tag Tags} associated with a request-response exchange that
 * is handled by Jersey server.
 *
 * @author Michael Weirauch
 * @author Johnny Lim
 * @since 2.41
 */
public final class JerseyTags {

    private static final Tag URI_NOT_FOUND = Tag.of("uri", "NOT_FOUND");

    private static final Tag URI_REDIRECTION = Tag.of("uri", "REDIRECTION");

    private static final Tag URI_ROOT = Tag.of("uri", "root");

    private static final Tag URI_UNKNOWN = Tag.of("uri", "UNKNOWN");

    private static final Tag EXCEPTION_NONE = Tag.of("exception", "None");

    private static final Tag STATUS_SERVER_ERROR = Tag.of("status", "500");

    private static final Tag METHOD_UNKNOWN = Tag.of("method", "UNKNOWN");

    static final Pattern TRAILING_SLASH_PATTERN = Pattern.compile("/$");

    static final Pattern MULTIPLE_SLASH_PATTERN = Pattern.compile("//+");

    private JerseyTags() {
    }

    /**
     * Creates a {@code method} tag based on the {@link ContainerRequest#getMethod()
     * method} of the given {@code request}.
     * @param request the container request
     * @return the method tag whose value is a capitalized method (e.g. GET).
     */
    public static Tag method(ContainerRequest request) {
        return (request != null) ? Tag.of("method", request.getMethod()) : METHOD_UNKNOWN;
    }

    /**
     * Creates a {@code status} tag based on the status of the given {@code response}.
     * @param response the container response
     * @return the status tag derived from the status of the response
     */
    public static Tag status(ContainerResponse response) {
        /* In case there is no response we are dealing with an unmapped exception. */
        return (response != null) ? Tag.of("status", Integer.toString(response.getStatus())) : STATUS_SERVER_ERROR;
    }

    /**
     * Creates a {@code uri} tag based on the URI of the given {@code event}. Uses the
     * {@link ExtendedUriInfo#getMatchedTemplates()} if available. {@code REDIRECTION} for
     * 3xx responses, {@code NOT_FOUND} for 404 responses.
     * @param event the request event
     * @return the uri tag derived from the request event
     */
    public static Tag uri(RequestEvent event) {
        ContainerResponse response = event.getContainerResponse();
        if (response != null) {
            int status = response.getStatus();
            if (isRedirection(status) && event.getUriInfo().getMatchedResourceMethod() == null) {
                return URI_REDIRECTION;
            }
            if (status == 404 && event.getUriInfo().getMatchedResourceMethod() == null) {
                return URI_NOT_FOUND;
            }
        }
        String matchingPattern = getMatchingPattern(event);
        if (matchingPattern == null) {
            return URI_UNKNOWN;
        }
        else if (matchingPattern.equals("/")) {
            return URI_ROOT;
        }
        return Tag.of("uri", matchingPattern);
    }

    static boolean isRedirection(int status) {
        return 300 <= status && status < 400;
    }

    static String getMatchingPattern(RequestEvent event) {
        ExtendedUriInfo uriInfo = event.getUriInfo();
        List<UriTemplate> templates = uriInfo.getMatchedTemplates();
        if (templates.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(uriInfo.getBaseUri().getPath());
        for (int i = templates.size() - 1; i >= 0; i--) {
            sb.append(templates.get(i).getTemplate());
        }
        String multipleSlashCleaned = MULTIPLE_SLASH_PATTERN.matcher(sb.toString()).replaceAll("/");
        if (multipleSlashCleaned.equals("/")) {
            return multipleSlashCleaned;
        }
        return TRAILING_SLASH_PATTERN.matcher(multipleSlashCleaned).replaceAll("");
    }

    /**
     * Creates an {@code exception} tag based on the {@link Class#getSimpleName() simple
     * name} of the class of the given {@code exception}.
     * @param event the request event
     * @return the exception tag derived from the exception
     */
    public static Tag exception(RequestEvent event) {
        Throwable exception = event.getException();
        if (exception == null) {
            return EXCEPTION_NONE;
        }
        ContainerResponse response = event.getContainerResponse();
        if (response != null) {
            int status = response.getStatus();
            if (status == 404 || isRedirection(status)) {
                return EXCEPTION_NONE;
            }
        }
        if (exception.getCause() != null) {
            exception = exception.getCause();
        }
        String simpleName = exception.getClass().getSimpleName();
        return Tag.of("exception", StringUtils.isNotEmpty(simpleName) ? simpleName : exception.getClass().getName());
    }

    /**
     * Creates an {@code outcome} tag based on the status of the given {@code response}.
     * @param response the container response
     * @return the outcome tag derived from the status of the response
     */
    public static Tag outcome(ContainerResponse response) {
        if (response != null) {
            return Outcome.forStatus(response.getStatus()).asTag();
        }
        /* In case there is no response we are dealing with an unmapped exception. */
        return Outcome.SERVER_ERROR.asTag();
    }

}
