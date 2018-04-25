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
 * Alternate version of test resource for osgi-helloworld-webapp example;
 * The original class within additional-bundle module has the same name and resides withing the same package.
 *
 * The containing module's (alternate-version-bundle) pom.xml configures the MANIFEST bundle headers as an older version of
 * additional-bundle.
 *
 * Both versions are then explicitly loaded into OSGi runtime and the correct version (the one in additional-bundle module)
 * should be used by Jersey.
 *
 * If this version of the resource is used, the test will fail.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
@Path("/additional")
public class AdditionalResource {
    @GET
    @Produces("text/plain")
    public String getAdditionalResourceMessage() {
        return "WRONG VERSION of additional Bundle!";
    }
}
