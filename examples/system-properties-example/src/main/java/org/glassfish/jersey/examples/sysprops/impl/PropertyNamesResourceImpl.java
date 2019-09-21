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

import java.util.Set;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.examples.sysprops.PropertyNamesResource;
import org.glassfish.jersey.examples.sysprops.PropertyResource;

/**
 * @author Martin Matula
 */
public class PropertyNamesResourceImpl implements PropertyNamesResource {
    @Context
    private UriInfo uriInfo;

    @Override
    public Set<String> getPropertyNames() {
        return System.getProperties().stringPropertyNames();
    }

    @Override
    public PropertyResource getProperty(String name) {
        return new PropertyResourceImpl(name, uriInfo);
    }
}
