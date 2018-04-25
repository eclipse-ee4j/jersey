/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainerProvider;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;

import org.hamcrest.CoreMatchers;
import org.jboss.weld.environment.se.Weld;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test two Jersey apps running simultaneously within a single Grizzly HTTP server
 * to make sure two injection managers do not interfere. The test is not executed
 * if other than the default (Grizzly) test container has been set.
 * For Servlet based container testing, the other two tests, {@link JaxRsInjectedCdiBeanTest}
 * and {@link SecondJaxRsInjectedCdiBeanTest},
 * do the same job, because the WAR application contains both Jersey apps already.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class NonJaxRsBeanJaxRsInjectionTest {

    public static final String MAIN_URI = "/main";
    public static final String SECONDARY_URI = "/secondary";

    public static final String PORT_NUMBER = getSystemProperty(TestProperties.CONTAINER_PORT,
                                                    Integer.toString(TestProperties.DEFAULT_CONTAINER_PORT));

    private static final URI MAIN_APP_URI = URI.create("http://localhost:" + PORT_NUMBER + MAIN_URI);
    private static final URI SECONDARY_APP_URI = URI.create("http://localhost:" + PORT_NUMBER + SECONDARY_URI);

    private static final boolean isDefaultTestContainerFactorySet = isDefaultTestContainerFactorySet();

    Weld weld;
    HttpServer httpServer;

    Client client;
    WebTarget mainTarget, secondaryTarget;

    @Before
    public void before() throws IOException {
        Assume.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());

        if (isDefaultTestContainerFactorySet) {
            initializeWeld();
            startGrizzlyContainer();
            initializeClient();
        }
    }

    @After
    public void after() {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            if (isDefaultTestContainerFactorySet) {
                httpServer.shutdownNow();
                weld.shutdown();
                client.close();
            }
        }
    }

    @Test
    public void testPathAndHeader() throws Exception {
        Assume.assumeThat(isDefaultTestContainerFactorySet, CoreMatchers.is(true));
        JaxRsInjectedCdiBeanTest._testPathAndHeader(mainTarget);
        SecondJaxRsInjectedCdiBeanTest._testPathAndHeader(secondaryTarget);
    }

    private void initializeWeld() {
        weld = new Weld();
        weld.initialize();
    }

    private void startGrizzlyContainer() throws IOException {
        final ResourceConfig firstConfig = ResourceConfig.forApplicationClass(MainApplication.class);
        final ResourceConfig secondConfig = ResourceConfig.forApplicationClass(SecondaryApplication.class);

        httpServer = GrizzlyHttpServerFactory.createHttpServer(MAIN_APP_URI, firstConfig, false);
        final HttpHandler secondHandler = createGrizzlyContainer(secondConfig);
        httpServer.getServerConfiguration().addHttpHandler(secondHandler, SECONDARY_URI);
        httpServer.start();
    }

    private GrizzlyHttpContainer createGrizzlyContainer(final ResourceConfig resourceConfig) {
        return (new GrizzlyHttpContainerProvider()).createContainer(GrizzlyHttpContainer.class, resourceConfig);
    }

    private void initializeClient() {
        client = ClientBuilder.newClient();
        mainTarget = client.target(MAIN_APP_URI);
        secondaryTarget = client.target(SECONDARY_APP_URI);
    }

    private static boolean isDefaultTestContainerFactorySet() {
        final String testContainerFactory = getSystemProperty(TestProperties.CONTAINER_FACTORY, null);
        return testContainerFactory == null || TestProperties.DEFAULT_CONTAINER_FACTORY.equals(testContainerFactory);
    }

    private static String getSystemProperty(final String propertyName, final String defaultValue) {
        final Properties systemProperties = System.getProperties();
        return systemProperties.getProperty(propertyName, defaultValue);
    }
}
