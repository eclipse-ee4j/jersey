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

package org.glassfish.jersey.server.model;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.glassfish.jersey.Severity;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Taken from Jersey 1: jersey-tests: com.sun.jersey.impl.errors.PathAndResourceMethodErrorsTest
 *
 * @author Paul Sandoz
 */
public class PathAndResourceMethodErrorsTest {

    private List<ResourceModelIssue> initiateWebApplication(Class<?>... resourceClasses) {
        return initiateWebApplication(new ResourceConfig(resourceClasses));
    }

    private List<ResourceModelIssue> initiateWebApplication(final ResourceConfig resourceConfig) {
        try {
            ApplicationHandler server = new ApplicationHandler(resourceConfig);
            fail("Application build expected to fail: " + server);
        } catch (ModelValidationException e) {
            return e.getIssues();
        }
        return null;
    }

    @Path("/{")
    public static class PathErrorsResource {

        @Path("/{")
        @GET
        public String get() {
            return null;
        }

        @Path("/{sub")
        public Object sub() {
            return null;
        }
    }

    // FIXME
    @Ignore
    @Test
    public void testPathErrors() {
        List<ResourceModelIssue> issues = initiateWebApplication(PathErrorsResource.class);
        assertEquals(3, issues.size());
    }

    //   TODO: testing not yet available feature (registering explicit resources).
    @Path("/{one}")
    public static class PathErrorsOneResource {
    }

    @Path("/{two}")
    public static class PathErrorsTwoResource {
    }

    @Path("/{three}")
    public static class PathErrorsThreeResource {
    }

    @Test
    @Ignore
    // TODO add cross-resource validation & un-ignore the test
    public void testConflictingRootResourceErrors() {
        ResourceConfig resourceConfig = new ResourceConfig(
                PathErrorsOneResource.class, PathErrorsTwoResource.class, PathErrorsThreeResource.class);

        resourceConfig.registerResources(Resource.builder(PathErrorsOneResource.class).path("/{four}").build());
        resourceConfig.registerResources(Resource.builder(PathErrorsThreeResource.class).path("/{five}").build());

        assertEquals(4, initiateWebApplication(resourceConfig));
    }

    @Test
    @Ignore
    // TODO add cross-resource validation & un-ignore the test
    public void testConflictingRootResourceErrors2() {
        ResourceConfig resourceConfig = new ResourceConfig();

        resourceConfig.registerResources(Resource.builder(PathErrorsOneResource.class).path("/{one}").build());
        resourceConfig.registerResources(Resource.builder(PathErrorsThreeResource.class).path("/{one}/").build());

        assertEquals(1, initiateWebApplication(resourceConfig));
    }

    @Path("/")
    public static class AmbiguousResourceMethodsGET {

        @GET
        public String get1() {
            return null;
        }

        @GET
        public String get2() {
            return null;
        }

        @GET
        public String get3() {
            return null;
        }
    }

    @Test
    public void testAmbiguousResourceMethodGET() {
        final List<ResourceModelIssue> issues = initiateWebApplication(AmbiguousResourceMethodsGET.class);
        assertEquals(3, issues.size());
    }

    @Path("/")
    public static class AmbiguousResourceMethodsProducesGET {

        @GET
        @Produces("application/xml")
        public String getXml() {
            return null;
        }

        @GET
        @Produces("text/plain")
        public String getText1() {
            return null;
        }

        @GET
        @Produces("text/plain")
        public String getText2() {
            return null;
        }

        @GET
        @Produces({"text/plain", "image/png"})
        public String getText3() {
            return null;
        }
    }

    @Test
    public void testAmbiguousResourceMethodsProducesGET() {
        final List<ResourceModelIssue> issues = initiateWebApplication(AmbiguousResourceMethodsProducesGET.class);
        assertEquals(3, issues.size());
    }

    @Path("/")
    public static class AmbiguousResourceMethodsConsumesPUT {

        @PUT
        @Consumes("application/xml")
        public void put1(Object o) {
        }

        @PUT
        @Consumes({"text/plain", "image/jpeg"})
        public void put2(Object o) {
        }

        @PUT
        @Consumes("text/plain")
        public void put3(Object o) {
        }
    }

    @Test
    public void testAmbiguousResourceMethodsConsumesPUT() {
        final List<ResourceModelIssue> issues = initiateWebApplication(AmbiguousResourceMethodsConsumesPUT.class);
        assertEquals(1, issues.size());
    }

    @Path("/")
    public static class AmbiguousSubResourceMethodsGET {

        @Path("{one}")
        @GET
        public String get1() {
            return null;
        }

        @Path("{seven}")
        @GET
        public String get2() {
            return null;
        }

        @Path("{million}")
        @GET
        public String get3() {
            return null;
        }

        @Path("{million}/")
        @GET
        public String get4() {
            return null;
        }
    }

    @Test
    public void testAmbiguousSubResourceMethodsGET() {
        final List<ResourceModelIssue> issues = initiateWebApplication(AmbiguousSubResourceMethodsGET.class);
        assertEquals(6, issues.size());
    }

    @Path("/")
    public static class AmbiguousSubResourceMethodsProducesGET {

        @Path("x")
        @GET
        @Produces("application/xml")
        public String getXml() {
            return null;
        }

        @Path("x")
        @GET
        @Produces("text/plain")
        public String getText1() {
            return null;
        }

        @Path("x")
        @GET
        @Produces("text/plain")
        public String getText2() {
            return null;
        }

        @Path("x")
        @GET
        @Produces({"text/plain", "image/png"})
        public String getText3() {
            return null;
        }
    }

    @Test
    public void testAmbiguousClassProducingWarnings() {

        final List<ResourceModelIssue> issues = initiateWebApplication(AmbiguousClassProducingWarnings.class);
        assertEquals(2, issues.size());
        assertNumberOfIssues(issues, 1, 1);
    }

    private void assertNumberOfIssues(final List<ResourceModelIssue> issues, int expectedFatalCount, int expectedWarningCount) {
        int fatalCount = 0;
        int warningCount = 0;

        for (ResourceModelIssue issue : issues) {
            if (issue.getSeverity() == Severity.FATAL) {
                fatalCount++;
            } else {
                warningCount++;
            }
        }
        assertEquals(expectedFatalCount, fatalCount);
        assertEquals(expectedWarningCount, warningCount);
    }

    @Path("test")
    public static class AmbiguousClassProducingWarnings {
        // GET methods validation will produce a warning
        @GET
        @Produces("text/plain")
        public String getHtml() {
            return null;
        }

        @GET
        public String getAllPossible() {
            return null;
        }

        // POST methods validation will fail
        @POST
        @Consumes("text/plain")
        public String postA() {
            return null;
        }

        @POST
        @Consumes("text/plain")
        public String postB() {
            return null;
        }
    }

    @Test
    public void testAmbiguousPostClassProducingWarnings() {

        final List<ResourceModelIssue> issues = initiateWebApplication(AmbiguousPostClassProducingWarnings.class);
        assertEquals(2, issues.size());
        assertNumberOfIssues(issues, 1, 1);
    }

    @Path("test2")
    public static class AmbiguousPostClassProducingWarnings {
        // GET methods validation will fail
        @GET
        @Produces("text/plain")
        public String getPlain() {
            return null;
        }

        @GET
        @Produces("text/plain")
        public String getPlain2() {
            return null;
        }


        // POST methods validation will produce a warning
        @POST
        @Consumes("text/plain")
        @Produces("text/plain")
        public String postA() {
            return null;
        }

        @POST
        @Consumes("text/plain")
        public String postB() {
            return null;
        }
    }

    @Test
    public void testAmbiguousSubResourceMethodsProducesGET() {
        final List<ResourceModelIssue> issues = initiateWebApplication(AmbiguousSubResourceMethodsProducesGET.class);
        assertEquals(3, issues.size());
    }

    @Path("/")
    public static class AmbiguousSubResourceLocatorsResource {

        @Path("{one}")
        public Object l1() {
            return null;
        }

        @Path("{two}")
        public Object l2() {
            return null;
        }
    }

    @Test
    public void testAmbiguousSubResourceLocatorsResource() {
        final List<ResourceModelIssue> issues = initiateWebApplication(AmbiguousSubResourceLocatorsResource.class);
        assertEquals(1, issues.size());
    }

    @Path("/")
    public static class AmbiguousSubResourceLocatorsWithSlashResource {

        @Path("{one}")
        public Object l1() {
            return null;
        }

        @Path("{two}/")
        public Object l2() {
            return null;
        }
    }

    // FIXME: trailing slashes should not matter
    @Ignore
    @Test
    public void testAmbiguousSubResourceLocatorsWithSlashResource() {
        final List<ResourceModelIssue> issues = initiateWebApplication(AmbiguousSubResourceLocatorsWithSlashResource.class);
        assertEquals(1, issues.size());
    }
}
