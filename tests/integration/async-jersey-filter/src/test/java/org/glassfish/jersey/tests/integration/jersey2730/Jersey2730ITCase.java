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

package org.glassfish.jersey.tests.integration.jersey2730;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.tests.integration.async.AbstractAsyncJerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * JERSEY-2730 reproducer.
 * <p/>
 * This test must not run in parallel.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
public class Jersey2730ITCase extends AbstractAsyncJerseyTest {

    private void assertLastThreadNotStuck() {
        final boolean lastThreadGotStuck = target("/exceptionTest/exception/rpc/lastthreadstuck").request().get(boolean.class);

        assertFalse("Thread processing last request got stuck while processing the request for "
                        + TestExceptionResource.class.getCanonicalName(),
                lastThreadGotStuck);
    }

    @Test
    public void asyncResourceNullThrowableReturns500AndDoesNotStuck() throws Exception  {
        final Response response = target("/exceptionTest/exception/null").request().get();

        assertEquals(500, response.getStatus());
        assertLastThreadNotStuck();
    }

    @Test
    public void asyncResourceUnmappedExceptionReturns500AndDoesNotStuck() throws Exception  {
        final Response response = target("/exceptionTest/exception/unmapped").request().get();

        assertEquals(500, response.getStatus());
        assertLastThreadNotStuck();
    }

    @Test
    public void asyncResourceUnmappedRuntimeExceptionReturns500AndDoesNotStuck() throws Exception  {
        final Response response = target("/exceptionTest/exception/runtime").request().get();

        assertEquals(500, response.getStatus());
        assertLastThreadNotStuck();
    }

    @Test
    public void asyncResourceMappedExceptionReturns432() throws Exception  {
        final Response response = target("/exceptionTest/exception/mapped").request().get();

        assertEquals(432, response.getStatus());
        assertLastThreadNotStuck();
    }

    @Test
    public void asyncResourceNonExistentReturns404() throws Exception {
        final Response response = target("/exceptionTest/exception/notfound").request().get();

        assertEquals(404, response.getStatus());
    }

}
