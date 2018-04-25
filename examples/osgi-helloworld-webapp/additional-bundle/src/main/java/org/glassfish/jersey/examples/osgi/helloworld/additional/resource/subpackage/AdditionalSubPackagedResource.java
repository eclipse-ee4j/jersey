/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.osgi.helloworld.additional.resource.subpackage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * This resource is located in a sub-package and will be detected by OSGI framework only if recursive scanning is turned on.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@Path("/subadditional")
public class AdditionalSubPackagedResource {

    @GET
    @Produces("text/plain")
    public String getSubPackagedAdditionalMessage() {
        return "Sub-packaged Additional Bundle!";
    }

}
