/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.osgi.helloworld.additional.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Test resource for osgi-helloworld-webapp example;
 * It is aimed to ensure, that the package scanning works in OSGi for multiple packages defined in web.xml
 *
 * There is also an alternate version of the resource with the same class name within the same package which
 * should not be visible to Jersey via OSGi.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@Path("/additional")
public class AdditionalResource {
    @GET
    @Produces("text/plain")
    public String getAdditionalResourceMessage() {
        return "Additional Bundle!";
    }
}
