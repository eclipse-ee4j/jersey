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

package org.glassfish.jersey.tests.integration.tracing;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.message.internal.TracingLogger;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.TracingConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 'ON_DEMAND' tracing support test that is running in external Jetty container.
 *
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class OnDemandTracingSupportITCase extends JerseyTest {

    //
    // JerseyTest
    //

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new OnDemandTracingSupport();
    }

    @Override
    protected void configureClient(ClientConfig clientConfig) {
        Utils.configure(clientConfig);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    //
    // tests
    //

    @Test
    public void testNoTracing() {
        Invocation.Builder builder = resource("/root").request();
        Response response = builder.post(Entity.entity(new Message("POST"), Utils.APPLICATION_X_JERSEY_TEST));
        assertFalse(hasX_Jersey_Trace(response));
        assertEquals("TSOP", response.readEntity(Message.class).getText());
    }

    @Test
    public void testTraceAcceptEmpty() {
        testTraceAccept("");
    }

    @Test
    public void testTraceAcceptTrue() {
        testTraceAccept("true");
    }

    @Test
    public void testTraceAcceptWhatever() {
        testTraceAccept("whatever");
    }

    private void testTraceAccept(String acceptValue) {
        Invocation.Builder builder = resource("/root").request();
        Response response = builder.header(TracingLogger.HEADER_ACCEPT, acceptValue)
                .post(Entity.entity(new Message("POST"), Utils.APPLICATION_X_JERSEY_TEST));
        assertTrue(hasX_Jersey_Trace(response));
        assertEquals("TSOP", response.readEntity(Message.class).getText());
    }

    //
    // utils
    //

    private boolean hasX_Jersey_Trace(Response response) {
        for (String k : response.getHeaders().keySet()) {
            if (k.toLowerCase().startsWith(Utils.HEADER_TRACING_PREFIX.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private WebTarget resource(String path) {
        return target("/" + TracingConfig.ON_DEMAND).path(path);
    }

}
