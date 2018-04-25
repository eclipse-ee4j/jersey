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

package org.glassfish.jersey.tests.e2e.server;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class InitializationLoggingTest extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        set(TestProperties.RECORD_LOG_LEVEL, Level.FINE.intValue());
        return new ResourceConfig(A.class);
    }

    @Path("test")
    public static class A {

        @GET
        public String get() {
            return "test";
        }
    }

    @Test
    public void test() {
        final Response r = target().path("test").request().get(Response.class);

        assertTrue(r.readEntity(String.class).contains("test"));

        boolean found = false;

        for (LogRecord logRecord : getLoggedRecords()) {
            if (logRecord.getMessage().contains(LocalizationMessages.LOGGING_APPLICATION_INITIALIZED())) {
                found = true;
                break;
            }
        }

        assertTrue(found);
    }
}
