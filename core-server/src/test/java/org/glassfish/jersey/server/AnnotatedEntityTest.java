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

package org.glassfish.jersey.server;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.glassfish.jersey.server.model.ModelValidationException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Tests entity parameter annotated with non injection annotations.
 *
 * @author Miroslav Fuksa
 *
 */
public class AnnotatedEntityTest {

    private ApplicationHandler createApplication(Class<?>... rc) {
        final ResourceConfig resourceConfig = new ResourceConfig(rc);
        return new ApplicationHandler(resourceConfig);
    }

    @Test
    public void testEntityAnnotated() throws ExecutionException, InterruptedException {
        ApplicationHandler app = createApplication(Resource.class);

        ContainerResponse res = app.apply(RequestContextBuilder.from("/resource/pathParam?query=queryParam",
                "POST").entity("entity").build()).get();
        assertEquals(200, res.getStatus());
        assertEquals("entity", res.getEntity());
    }

    @Test
    public void testAllAnnotated() throws ExecutionException, InterruptedException {
        ApplicationHandler app = createApplication(Resource.class);

        ContainerResponse res = app.apply(RequestContextBuilder.from("/resource/pathParam/allAnnotated?query=queryParam",
                "POST").entity("entity").build()).get();
        assertEquals(200, res.getStatus());
        assertEquals("entity", res.getEntity());
    }

    @Test
    public void testContextAnnotated() throws ExecutionException, InterruptedException {
        ApplicationHandler app = createApplication(Resource.class);

        ContainerResponse res = app.apply(RequestContextBuilder.from("/resource/pathParam/context?query=queryParam",
                "POST").entity("entity").build()).get();
        assertEquals(200, res.getStatus());
        assertEquals("entity", res.getEntity());
    }

    @Test
    public void testAmbiguousEntityParameter() throws ExecutionException, InterruptedException {
        try {
            ApplicationHandler app = createApplication(WrongResource.class);
            fail("Model validation should fail.");
        } catch (ModelValidationException ex) {
            // ok - should be thrown
        } catch (Exception e) {
            fail("ModelValidationException should be thrown.");
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface TestAnnotation {
    }

    @Path("resource/{path}")
    public static class Resource {

        @POST
        public String postEntityAnnotated(@PathParam("path") String pathParam, @TestAnnotation String entity,
                                          @QueryParam("query") String queryParam) {

            testParameters(pathParam, entity, queryParam);
            return entity;
        }

        private void testParameters(String pathParam, String entity, String queryParam) {
            assertEquals("pathParam", pathParam);
            assertEquals("queryParam", queryParam);
            assertEquals("entity", entity);
        }

        @POST
        @Path("allAnnotated")
        public String postAllAnnotated(@TestAnnotation @PathParam("path") String pathParam, @TestAnnotation String entity,
                                       @TestAnnotation @QueryParam("query") String queryParam) {

            testParameters(pathParam, entity, queryParam);
            return entity;
        }

        @POST
        @Path("context")
        public String postContextAnnotation(@PathParam("path") String pathParam, @Context HttpHeaders headers,
                                            @TestAnnotation String entity, @QueryParam("query") String queryParam) {
            testParameters(pathParam, entity, queryParam);
            assertNotNull(headers);
            return entity;
        }
    }

    @Path("wrongResource/{path}")
    public static class WrongResource {

        @POST
        public String postEntityAnnotated(@PathParam("path") String pathParam, @TestAnnotation String ambiguousEntity,
                                          String ambiguousParameter,
                                          @QueryParam("query") String queryParam) {

            fail("Should not be called (ambiguous entity parameter).");
            return null;
        }

    }
}
