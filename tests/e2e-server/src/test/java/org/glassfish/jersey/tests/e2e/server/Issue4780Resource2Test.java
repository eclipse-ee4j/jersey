/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.ModelValidationException;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

public class Issue4780Resource2Test {

    // 2 interfaces having same @Path
    @Test
    public void resource2() throws Exception {
        assertThrows(ModelValidationException.class, () -> {
            JerseyTest test = new JerseyTest(new ResourceConfig(IResource2_1.class, IResource2_2.class)) {
            };
            try {
                test.setUp();
            } finally {
                test.tearDown();
            }
        });
    }

    @Path("")
    public static interface IResource2_1 {
        @GET
        @Path("/resource2")
        String get();
    }

    @Path("")
    public static interface IResource2_2 {
        @GET
        @Path("/resource2")
        String get();
    }

}
