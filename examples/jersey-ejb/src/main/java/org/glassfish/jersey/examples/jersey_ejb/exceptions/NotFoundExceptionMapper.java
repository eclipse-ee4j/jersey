/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jersey_ejb.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An exception mapper to return 404 responses when a {@link CustomNotFoundException} is thrown.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<CustomNotFoundException> {

    @Override
    public Response toResponse(CustomNotFoundException exception) {
        return Response.status(404).build();
    }

}
