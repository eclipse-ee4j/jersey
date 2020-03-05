/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

/**
 * @author Michal Gajdos
 */
public class ExtendedNotFoundException extends NotFoundException {

    public ExtendedNotFoundException(final String message) {
        super(Response.status(Response.Status.NOT_FOUND).entity(message).build());
    }
}
