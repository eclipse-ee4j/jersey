/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import javax.json.Json;
import javax.json.JsonObject;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.osgi.test.util.Helper;
import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Basic test of Json Processing.
 *
 * @author Michal Gajdos
 */
@RunWith(PaxExam.class)
public class JsonProcessingTest {

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

                // JSON processing.
                mavenBundle().groupId("org.glassfish.jersey.media").artifactId("jersey-media-json-processing")
                        .versionAsInProject(),
                mavenBundle().groupId("org.glassfish").artifactId("javax.json").versionAsInProject(),
                mavenBundle().groupId("org.glassfish").artifactId("jsonp-jaxrs").versionAsInProject()
        ));

        options = Helper.addPaxExamMavenLocalRepositoryProperty(options);
        return Helper.asArray(options);
    }

    @Path("/")
    public static class Resource {

        @POST
        public JsonObject postJsonObject(final JsonObject jsonObject) {
            return jsonObject;
        }
    }

    @Test
    public void testJsonObject() throws Exception {
        final ResourceConfig resourceConfig = new ResourceConfig(Resource.class);
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);
        final JsonObject jsonObject = Json.createObjectBuilder().add("foo", "bar").build();

        final Client client = ClientBuilder.newClient();
        final JsonObject entity = client
                .target(baseUri)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(jsonObject), JsonObject.class);

        System.out.println("RESULT = " + entity);
        assertEquals(jsonObject, entity);

        server.shutdownNow();
    }
}
