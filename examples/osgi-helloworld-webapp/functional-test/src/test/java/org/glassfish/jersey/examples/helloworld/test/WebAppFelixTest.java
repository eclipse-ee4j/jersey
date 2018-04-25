/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.helloworld.test;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
public class WebAppFelixTest extends AbstractWebAppTest {

    private static final Logger LOGGER = Logger.getLogger(WebAppFelixTest.class.getName());

    @Override
    public List<Option> osgiRuntimeOptions() {
        return Arrays.asList(CoreOptions.options(
                mavenBundle()
                        .groupId("org.apache.felix").artifactId("org.apache.felix.eventadmin")
                        .versionAsInProject()
        )
        );
    }

    @Before
    public void before() throws Exception {
        defaultMandatoryBeforeMethod();
    }

    @Test
    public void testWebResources() throws Exception {
        final WebTarget target = webAppTestTarget("/webresources");

        // send request and check response - helloworld resource
        final String helloResult = target.path("/helloworld").request().build("GET").invoke().readEntity(
                String.class);
        LOGGER.info("HELLO RESULT = " + helloResult);
        assertEquals("Hello World", helloResult);

        // send request and check response - another resource
        final String anotherResult = target.path("/another").request().build("GET").invoke()
                .readEntity(String.class);

        LOGGER.info("ANOTHER RESULT = " + anotherResult);
        assertEquals("Another", anotherResult);

        // send request and check response for the additional bundle - should fail now
        final String additionalResult = target.path("/additional").request().build("GET").invoke()
                .readEntity(String.class);

        LOGGER.info("ADDITIONAL RESULT = " + additionalResult);
        assertEquals("Additional Bundle!", additionalResult);

        // send request and check response for the sub-packaged additional bundle
        final String subAdditionalResult = target.path("/subadditional").request().build("GET").invoke()
                .readEntity(String.class);

        LOGGER.info("SUB-PACKAGED ADDITIONAL RESULT = " + subAdditionalResult);
        assertEquals("Sub-packaged Additional Bundle!", subAdditionalResult);

        // send request and check response for the WEB-INF classes located resource
        final String webInfClassesResourceResult = target.path("/webinf").request().build("GET").invoke()
                .readEntity(String.class);

        LOGGER.info("WEB-INF CLASSES RESOURCE RESULT = " + webInfClassesResourceResult);
        assertEquals("WebInfClassesResource", webInfClassesResourceResult);

        // send request and check response for the WEB-INF classes located resource
        final String webInfClassesSubPackagedResourceResult = target.path("/subwebinf").request().build("GET")
                .invoke().readEntity(String.class);

        LOGGER.info("WEB-INF CLASSES SUB-PACKAGED RESOURCE RESULT = " + webInfClassesSubPackagedResourceResult);
        assertEquals("WebInfClassesSubPackagedResource", webInfClassesSubPackagedResourceResult);
    }

    @Test
    public void testNonRecursiveWebResources() throws Exception {
        final WebTarget target = webAppTestTarget("/n-webresources");

        // send request and check response - helloworld resource
        final String helloResult = target.path("/helloworld").request().build("GET").invoke().readEntity(
                String.class);
        LOGGER.info("HELLO RESULT = " + helloResult);
        assertEquals("Hello World", helloResult);

        // send request and check response - another resource
        final String anotherResult = target.path("/another").request().build("GET").invoke()
                .readEntity(String.class);

        LOGGER.info("ANOTHER RESULT = " + anotherResult);
        assertEquals("Another", anotherResult);

        // send request and check response for the additional bundle - should fail now
        final String additionalResult = target.path("/additional").request().build("GET").invoke()
                .readEntity(String.class);

        LOGGER.info("ADDITIONAL RESULT = " + additionalResult);
        assertEquals("Additional Bundle!", additionalResult);

        // send request and check response for the sub-packaged additional bundle
        final Response subAdditionalResponse = target.path("/subadditional").request().build("GET").invoke();

        LOGGER.info("SUB-PACKAGED ADDITIONAL http status = " + subAdditionalResponse.getStatus());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), subAdditionalResponse.getStatus());

        // send request and check response for the WEB-INF classes located resource
        final String webInfClassesResourceResult = target.path("/webinf").request().build("GET").invoke()
                .readEntity(String.class);

        LOGGER.info("WEB-INF CLASSES RESOURCE RESULT = " + webInfClassesResourceResult);
        assertEquals("WebInfClassesResource", webInfClassesResourceResult);

        // send request and check response for the WEB-INF classes located resource
        final Response webInfClassesSubPackagedResourceResponse = target.path("/subwebinf").request().build("GET")
                .invoke();

        LOGGER.info("WEB-INF CLASSES SUB-PACKAGED http status = " + webInfClassesSubPackagedResourceResponse.getStatus());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), webInfClassesSubPackagedResourceResponse.getStatus());
    }
}
