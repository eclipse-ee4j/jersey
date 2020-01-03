/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.glassfish.jersey.server.ContainerResponse;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Paul Sandoz
 * @author Pavel Bucek
 */
public class PathParamStringConstructorTest extends AbstractTest {

    public PathParamStringConstructorTest() {
        initiateWebApplication(Resource.class);
    }

    @Path("/{a}/{b}")
    public static class Resource {
        @GET
        public String doGet(
                @PathParam("a") BigDecimal a,
                @PathParam("b") BigInteger b) {
            assertEquals("3.145", a.toString());
            assertEquals("3145", b.toString());
            return "content";
        }
    }

    @Test
    public void testStringConstructorGet() throws ExecutionException, InterruptedException {
        _test("/3.145/3145");
    }

    @Test
    public void testBadStringConstructorValue() throws ExecutionException, InterruptedException {
        final ContainerResponse responseContext = getResponseContext("/ABCDE/ABCDE");

        assertEquals(404, responseContext.getStatus());
    }
}
