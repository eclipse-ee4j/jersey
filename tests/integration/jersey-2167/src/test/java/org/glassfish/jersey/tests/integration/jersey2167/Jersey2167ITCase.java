/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2167;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Reproducer tests for JERSEY-2167.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public class Jersey2167ITCase extends JerseyTest {

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new Jersey2167App();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testResourceMethodCall() throws Exception {
        Response response = target().path("MyResource/test").request().get();
        // if parameter was not injected, resource returns Response.Status.SERVER_ERROR (500). Missing parameter means,
        // that hk2 injected the parameter and invoked the method preliminary and during Jersey-driven invocation,
        // there was no parameter injection any more. If parameter was injected and Response.Status.OK (200) returned,
        // the resource method was called only once (by Jersey).
        assertEquals("Parameter not injected into resource method. Resource method could have been called twice. ",
                response.getStatus(), Response.Status.OK.getStatusCode());
    }
}
