/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.ExecutionException;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests injections into constructor parameters.
 *
 * @author Miroslav Fuksa
 */
public class ConstructorParameterInjectionTest extends AbstractTest {
    @Test
    public void testInjection() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceWithConstructor.class);

        _test("/resource/p;matrix=m?query=5");
    }

    @Test
    public void testInjectionIntoFields() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceWithFieldInjections.class);

        _test("/fields/p;matrix=m?query=5");
    }


    @Test
    public void testInjectionsWithoutMatrix() throws ExecutionException, InterruptedException {
        initiateWebApplication(ResourceWithConstructor.class);

        _test("/resource/p/nullMatrix/?query=5");
    }

    @Path("resource/{path}")
    public static class ResourceWithConstructor {
        private final String pathParam;
        private final int queryParam;
        private final String matrixParam;

        public ResourceWithConstructor(@PathParam("path") String pathParam, @QueryParam("query") int queryParam,
                                       @MatrixParam("matrix") String matrixParam) {
            this.matrixParam = matrixParam;
            this.pathParam = pathParam;
            this.queryParam = queryParam;
        }

        @GET
        public String get() {
            assertEquals("p", pathParam);
            assertEquals(5, queryParam);
            assertEquals("m", matrixParam);
            return "content";
        }

        @GET
        @Path("nullMatrix")
        public String getWithoutMatrixParam() {
            assertEquals("p", pathParam);
            assertEquals(5, queryParam);
            assertNull(matrixParam);
            return "content";
        }
    }


    @Path("fields/{path}")
    public static class ResourceWithFieldInjections {
        @PathParam("path")
        private String pathParam;
        @QueryParam("query")
        private int queryParam;
        @MatrixParam("matrix")
        private String matrixParam;


        @GET
        public String get() {
            assertEquals("p", pathParam);
            assertEquals(5, queryParam);
            assertEquals("m", matrixParam);
            return "content";
        }
    }

}
