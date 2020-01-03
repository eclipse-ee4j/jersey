/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_3_init_provider;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;

/**
 * @author Libor Kramolis
 */
public abstract class AbstractHelloWorldResource {

    public static final int NUMBER_OF_APPLICATIONS = 5;

    @GET
    @Produces("text/plain")
    public Hello get() {
        return new Hello(createName());
    }

    protected abstract String createName();


    public static class Hello {
        private final String name;

        public Hello(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
