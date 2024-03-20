/*
 * Copyright (c) 2024. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.osgi.helloworld;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * The Hello resource
 *
 * @author Gregor Pfeifer
 */
@Path("/hello")
public class HelloResource {

    @GET
    public String sayHello() {
        return "Hello world :-)";
    }

}
