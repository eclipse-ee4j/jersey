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
 * A contract for {@link RequestEvent request monitoring event} builder.
 *
 * @author Miroslav Fuksa
 */
public interface RequestEventBuilder {

    /**
     * Set the exception mapper.
     *
     * @param exceptionMapper Exception mapper.
     * @return Builder instance.
     */
    public RequestEventBuilder setExceptionMapper(ExceptionMapper<?> exceptionMapper);

    /**
     * Set the container request.
     *
     * @param containerRequest Container request.
     * @return Builder instance.
     */
    public RequestEventBuilder setContainerRequest(ContainerRequest containerRequest);

    /**
     * Set the container response.
     *
     * @param containerResponse Container response.
     * @return Builder instance.
     */
    public RequestEventBuilder setContainerResponse(ContainerResponse containerResponse);

    /**
     * Set the flag indicating whether the response processing was successful. Set {@code true}
     * if the request and response has been successfully processed. Response is successfully
     * processed when the response code is smaller than 400 and response was successfully written.
     *
     * @param success True if response processing was successful.
     * @return Builder instance.
     *
     * @see RequestEvent#isSuccess()
     */
    public RequestEventBuilder setSuccess(boolean success);

    /**
     * Set the flag indicating whether response has been successfully written.
     *
     * @param responseWritten {@code true} is response has been written without failure.
     * @return Builder instance.
     */
    public RequestEventBuilder setResponseWritten(boolean responseWritten);

    /**
     * Set exception thrown.
     *
     * @param throwable      Exception.
     * @param exceptionCause Cause of the {@code throwable}
     * @return Builder instance.
     */
    public RequestEventBuilder setException(Throwable throwable, RequestEvent.ExceptionCause exceptionCause);

    /**
     * Set uri info.
     *
     * @param extendedUriInfo Extended uri info.
     * @return Builder instance.
     */
    public RequestEventBuilder setExtendedUriInfo(ExtendedUriInfo extendedUriInfo);

    /**
     * Set response filters.
     *
     * @param containerResponseFilters Container response filters.
     * @return Builder instance.
     */
    public RequestEventBuilder setContainerResponseFilters(Iterable<ContainerResponseFilter> containerResponseFilters);

    /**
     * Set request filters.
     *
     * @param containerRequestFilters Container request filters.
     * @return Request filters.
     */
    public RequestEventBuilder setContainerRequestFilters(Iterable<ContainerRequestFilter> containerRequestFilters);

    /**
     * Set the flag indicating whether the response has been successfully mapped by an exception mapper.
     *
     * @param responseSuccessfullyMapped {@code true} if the response has been successfully mapped.
     * @return Builder instance.
     */
    public RequestEventBuilder setResponseSuccessfullyMapped(boolean responseSuccessfullyMapped);

    /**
     * Build the instance of {@link RequestEvent request event}.
     *
     * @param eventType Type of the event to be built.
     * @return Request event instance.
     */
    public RequestEvent build(RequestEvent.Type eventType);
}
