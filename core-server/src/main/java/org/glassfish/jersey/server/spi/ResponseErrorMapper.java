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

package org.glassfish.jersey.server.spi;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.spi.Contract;

/**
 * Contract for a provider that maps response processing errors to {@link javax.ws.rs.core.Response}.
 * <p>
 * Providers implementing {@code ResponseErrorMapper} contract must be either programmatically registered in a JAX-RS runtime or
 * must be annotated with {@link javax.ws.rs.ext.Provider &#64;Provider} annotation to be automatically discovered by the JAX-RS
 * runtime during a provider scanning phase.
 * </p>
 * <p>
 * {@link org.glassfish.jersey.server.ServerProperties#PROCESSING_RESPONSE_ERRORS_ENABLED} property has to be enabled in order to
 * use this contract.
 * </p>
 *
 * @author Michal Gajdos
 * @see javax.ws.rs.ext.Provider
 * @see javax.ws.rs.core.Response
 * @since 2.8
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ResponseErrorMapper {

    public Response toResponse(Throwable throwable);
}
