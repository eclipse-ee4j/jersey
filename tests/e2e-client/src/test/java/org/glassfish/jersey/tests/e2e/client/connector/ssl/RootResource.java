/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.client.connector.ssl;

import java.util.Base64;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;


/**
 * Simple resource demonstrating low level approach of getting user credentials.
 *
 * A better way would be injecting {@link javax.ws.rs.core.SecurityContext}.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
@Path("/")
public class RootResource {
    private static final Logger LOGGER = Logger.getLogger(RootResource.class.getName());
    /**
     * Served content.
     */
    public static final String CONTENT = "JERSEY HTTPS EXAMPLE\n";

    /**
     * Serve content.
     *
     * @param headers request headers.
     * @return content (see {@link #CONTENT}).
     */
    @GET
    public String getContent(@Context HttpHeaders headers) {
        // you can get username form HttpHeaders
        LOGGER.info("Service: GET / User: " + getUser(headers));

        return CONTENT;
    }

    private String getUser(HttpHeaders headers) {
        // this is a very minimalistic and "naive" code;
        // if you plan to use it, add the necessary checks
        String auth = headers.getRequestHeader("authorization").get(0);

        auth = auth.substring("Basic ".length());
        String[] values = new String(Base64.getDecoder().decode(auth)).split(":");

        // String username = values[0];
        // String password = values[1];
        return values[0];
    }
}
