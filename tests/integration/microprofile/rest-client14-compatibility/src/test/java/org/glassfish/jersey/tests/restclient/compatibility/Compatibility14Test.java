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

package org.glassfish.jersey.tests.restclient.compatibility;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.jboss.weld.environment.se.Weld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Compatibility14Test extends JerseyTest {
    private Weld weld;

    @BeforeEach
    public void setup() {
        Assumptions.assumeTrue(Hk2InjectionManagerFactory.isImmediateStrategy());
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            weld = new Weld();
            weld.initialize();
            super.setUp();
        }
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        if (Hk2InjectionManagerFactory.isImmediateStrategy()) {
            weld.shutdown();
            super.tearDown();
        }
    }

    @RegisterRestClient
    public static interface CompatibilityClient {
        @GET
        public String get();
    }

    @Path("/resource")
    @RequestScoped
    public static class CompatibilityResource {

        @Inject
        @RestClient
        CompatibilityClient client;

        @GET
        public String get() {
            return client.get();
        }
    }

    @Path("/inner")
    public static class InnerResource implements CompatibilityClient {

        public String get() {
            return "INNER";
        }
    }

    @Override
    protected Application configure() {
        set(TestProperties.RECORD_LOG_LEVEL, Level.WARNING.intValue());
        return new ResourceConfig(InnerResource.class, CompatibilityResource.class)
                .property(ServerProperties.WADL_FEATURE_DISABLE, true);
    }

    @Test
    public void testCompatibility() {
        final String loggerName = "org.glassfish.jersey.microprofile.restclient.VersionSupport";

        try (Response r = target("/resource").request().get()) {
            String entity = r.readEntity(String.class);
            Assertions.assertEquals(new InnerResource().get(), entity);
        }

        int warningCounts = 0;
        for (final LogRecord logRecord : getLoggedRecords()) {
            if (loggerName.equals(logRecord.getLoggerName()) && logRecord.getLevel() == Level.WARNING) {
                warningCounts++;
            }
        }

        Assertions.assertEquals(3, warningCounts);
    }
}
