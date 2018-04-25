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

package org.glassfish.jersey.tests.cdi.resources;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;


/**
 * GF-21033 reproducer. Just a resource using multi-part support.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("/echo")
@RequestScoped
public class EchoResource {

    /**
     * We want to consume form data using multi-part provider.
     *
     * @param input form data
     * @return input data
     */
    @POST
    public Response echoMultipart(@FormDataParam ("input") String input) {
        return Response.ok(input).build();
    }
}
