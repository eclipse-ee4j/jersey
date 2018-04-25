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

package org.glassfish.jersey.osgi.test.basic;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.osgi.test.util.Helper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * NOTE: This test is excluded on JDK6 as it requires Servlet 3.1 API that is built against JDK 7.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos
 */
@RunWith(PaxExam.class)
public class PackageScanningTest {

    private static final String CONTEXT = "/jersey";

    private static final URI baseUri = UriBuilder
            .fromUri("http://localhost")
            .port(Helper.getPort())
            .path(CONTEXT).build();

    @Configuration
    public static Option[] configuration() {
        List<Option> options = Helper.getCommonOsgiOptions();

        options.addAll(Helper.expandedList(
                // vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),

                mavenBundle().groupId("org.glassfish.jersey.media").artifactId("jersey-media-sse").versionAsInProject(),

                mavenBundle().groupId("javax.servlet").artifactId("javax.servlet-api").versionAsInProject(),
                mavenBundle().groupId("org.glassfish.grizzly").artifactId("grizzly-http-servlet").versionAsInProject(),
                mavenBundle().groupId("org.glassfish.jersey.containers").artifactId("jersey-container-servlet-core")
                        .versionAsInProject(),
                mavenBundle().groupId("org.glassfish.jersey.containers").artifactId("jersey-container-grizzly2-servlet")
                        .versionAsInProject(),

                // MBR/MBW for JSON-P is on the classpath.
                mavenBundle().groupId("org.glassfish").artifactId("javax.json").versionAsInProject()
        ));

        options = Helper.addPaxExamMavenLocalRepositoryProperty(options);
        return Helper.asArray(options);
    }

    @Test
    public void testSimpleResource() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig().packages(SimpleResource.class.getPackage().getName());
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

        _testScannedResources(server);
    }

    @Test
    public void testSimpleResourceInitParameters() throws Exception {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(
                ServerProperties.PROVIDER_PACKAGES,
                SimpleResource.class.getPackage().getName());

        // TODO - temporary workaround
        // This is a workaround related to issue JERSEY-2093; grizzly (1.9.5) needs to have the correct context
        // classloader set
        ClassLoader myClassLoader = getClass().getClassLoader();
        ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
        HttpServer server = null;
        try {
            Thread.currentThread().setContextClassLoader(myClassLoader);
            server = GrizzlyWebContainerFactory.create(baseUri, ServletContainer.class, initParams);
        } finally {
            Thread.currentThread().setContextClassLoader(originalContextClassLoader);
        }
        // END of workaround - when grizzly updated to more recent version, only the inner line of try clause should remain:

        _testScannedResources(server);
    }

    private void _testScannedResources(final HttpServer server) throws Exception {
        final Client client = ClientBuilder.newClient();

        assertEquals("OK", client.target(baseUri).path("/simple").request().get(String.class));
        // resources in subpackages aren't supported yet because the osgi recursive scanning is set to false
//        assertEquals("sub-OK", client.target(baseUri).path("/sub-packaged").request().get(String.class));

        server.shutdownNow();
    }

}
