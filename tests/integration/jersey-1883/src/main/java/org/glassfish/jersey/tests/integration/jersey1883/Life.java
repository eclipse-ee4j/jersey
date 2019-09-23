/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey1883;

import javax.annotation.PostConstruct;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application mixed with resource.
 *
 * @author Libor Kramolis
 */
@ApplicationPath("/rest3")
@Path("/life")
public class Life extends Application {

    private static int postConstructCount = 0;

    @PostConstruct
    public void post_construct() {
        postConstructCount++;
    }

    @GET
    public String greet() {
        return "hi #" + postConstructCount;
    }
}
