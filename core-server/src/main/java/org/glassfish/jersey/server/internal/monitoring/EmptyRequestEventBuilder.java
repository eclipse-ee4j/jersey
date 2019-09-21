/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.monitoring;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.ExceptionMapper;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.monitoring.RequestEvent;

/**
 * Empty mock implementation of {@link RequestEventBuilder}.
 *
 * @author Miroslav Fuksa
 */
public class EmptyRequestEventBuilder implements RequestEventBuilder {

    /**
     * Instance of empty request event builder.
     */
    public static final EmptyRequestEventBuilder INSTANCE = new EmptyRequestEventBuilder();

    @Override
    public RequestEventBuilder setExceptionMapper(ExceptionMapper<?> exceptionMapper) {
        return this;
    }

    @Override
    public RequestEventBuilder setContainerRequest(ContainerRequest containerRequest) {
        return this;
    }

    @Override
    public RequestEventBuilder setContainerResponse(ContainerResponse containerResponse) {
        return this;
    }

    @Override
    public RequestEventBuilder setSuccess(boolean success) {
        return this;
    }

    @Override
    public RequestEventBuilder setResponseWritten(boolean responseWritten) {
        return this;
    }

    @Override
    public RequestEventBuilder setException(Throwable throwable, RequestEvent.ExceptionCause exceptionCause) {
        return this;
    }

    @Override
    public RequestEventBuilder setExtendedUriInfo(ExtendedUriInfo extendedUriInfo) {
        return this;
    }

    @Override
    public RequestEventBuilder setContainerResponseFilters(Iterable<ContainerResponseFilter> containerResponseFilters) {
        return this;
    }

    @Override
    public RequestEventBuilder setContainerRequestFilters(Iterable<ContainerRequestFilter> containerRequestFilters) {
        return this;
    }

    @Override
    public RequestEventBuilder setResponseSuccessfullyMapped(boolean responseSuccessfullyMapped) {
        return this;
    }

    @Override
    public RequestEvent build(RequestEvent.Type eventType) {
        return null;
    }
}
