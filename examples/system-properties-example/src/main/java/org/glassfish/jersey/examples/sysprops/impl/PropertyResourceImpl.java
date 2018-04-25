/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.sysprops.impl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.examples.sysprops.PropertyResource;

/**
 * @author Martin Matula
 */
public class PropertyResourceImpl implements PropertyResource {
    private final String name;
    private final UriInfo uriInfo;

    public PropertyResourceImpl(String name, UriInfo uriInfo) {
        this.name = name;
        this.uriInfo = uriInfo;
    }

    @Override
    public String get() {
        String value = System.getProperty(name);
        if (value == null) {
            throw new WebApplicationException(404);
        }
        return value;
    }

    @Override
    public String set(String value) {
        if (System.setProperty(name, value) == null) {
            throw new WebApplicationException(Response.created(uriInfo.getRequestUri()).entity(value).build());
        }
        return value;
    }
}
