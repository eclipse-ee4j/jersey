/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
 * Copyright (c) 2012, 2024 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.osgi.test.util.Helper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationFeature;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * Basic test of Bean Validation.
 *
 * @author Michal Gajdos
 * @author Libor Kramolis
 */
@RunWith(PaxExam.class)
public class BeanValidationTest {

    private static final String CONTEXT = "/jersey";

    private static final URI baseUri = UriBuilder.fromUri("http://localhost")
            .port(Helper.getPort())
            .path(CONTEXT).build();

    @Configuration
    public static Option[] configuration() {
        List<Option> options = Helper.getCommonOsgiOptions();

        options.addAll(Helper.expandedList(
                // for debug purposes
                // PaxRunnerOptions.vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"),
                systemProperty("org.ops4j.pax.logging.jbosslogging.disabled").value("true"),

                // validation
                mavenBundle().groupId("org.glassfish.jersey.ext").artifactId("jersey-bean-validation").versionAsInProject(),
                mavenBundle().groupId("org.hibernate.validator").artifactId("hibernate-validator").versionAsInProject(),
                mavenBundle().groupId("org.jboss.logging").artifactId("jboss-logging").version("3.6.1.Final"),
                mavenBundle().groupId("com.fasterxml").artifactId("classmate").versionAsInProject(),
                mavenBundle().groupId("jakarta.el").artifactId("jakarta.el-api").versionAsInProject(),
                mavenBundle().groupId("org.glassfish.expressly").artifactId("expressly").versionAsInProject()

        ));

        options = Helper.addPaxExamMavenLocalRepositoryProperty(options);
        return Helper.asArray(options);
    }

    @Test
    public void testBeanValidationResourceFeature() throws Exception {
        _test(400, true, false);
    }

    @Test
    public void testBeanValidationResourceAutoDiscovery() throws Exception {
        _test(400, false, false);
    }

    @Test
    public void testBeanValidationResourceManualRegistration() throws Exception {
        _test(400, true, true);
    }

    @Test
    public void testBeanValidationResourceNoValidationFeature() throws Exception {
        // Even though properties are disabled BV is registered.
        _test(400, false, true);
    }

    protected void _test(final int expectedResponseCode,
                         final boolean registerFeature,
                         final boolean disableMetainfServicesLookup) {
        final ResourceConfig resourceConfig = new ResourceConfig(BeanValidationResource.class);
        if (registerFeature) {
            resourceConfig.register(ValidationFeature.class);
        }
        if (disableMetainfServicesLookup) {
            resourceConfig.property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, Boolean.TRUE);

            resourceConfig.register(org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainerProvider.class);
        }

        System.out.println("Logger class = " + org.jboss.logging.Logger.class);
        System.out.println("Logger resource = " + org.jboss.logging.Logger.class.getResource("Logger.class"));

        try {
            System.out.println("Expected method = " + org.jboss.logging.Logger.class.getMethod(
                "getMessageLogger",
                java.lang.invoke.MethodHandles.Lookup.class,
                Class.class,
                String.class
            ));
        } catch (NoSuchMethodException e) {
            System.out.println("MISSING getMessageLogger(Lookup, Class, String)");
        }

        org.osgi.framework.Bundle lb = org.osgi.framework.FrameworkUtil.getBundle(org.jboss.logging.Logger.class);
        System.out.println("Logger bundle = " + lb);
        System.out.println("Logger bundle version = " + (lb != null ? lb.getVersion() : "null"));

        org.osgi.framework.Bundle hvb = org.osgi.framework.FrameworkUtil.getBundle(
                org.hibernate.validator.HibernateValidator.class);
        System.out.println("HV bundle = " + hvb);
        System.out.println("HV bundle version = " + (hvb != null ? hvb.getVersion() : "null"));


        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

        final Form form = new Form();
        final String formValue = "formValue";
        form.asMap().add("formParam", formValue);

        final Client client = ClientBuilder.newClient();
        final String entity = client.target(baseUri)
                .path("/bean-validation")
                .request()
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE), String.class);

        assertEquals(formValue, entity);

        final Response response = client.target(baseUri)
                .path("/bean-validation")
                .request()
                .post(Entity.entity(new Form(), MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertEquals(expectedResponseCode, response.getStatus());

        server.shutdownNow();
    }

}
