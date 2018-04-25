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

package org.glassfish.jersey.server.oauth1;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link OAuth1Exception} unit tests.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class OAuth1ExceptionTest {
    /**
     * Reproducer for JERSEY-2457.
     */
    @Test
    public void testExceptionGetters() {
        OAuth1Exception exception;

        exception = new OAuth1Exception(Response.Status.BAD_REQUEST, null);
        assertEquals(Response.Status.BAD_REQUEST, exception.getStatus());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
        assertNull(exception.getWwwAuthHeader());
        assertNull(exception.getResponse().getHeaderString(HttpHeaders.WWW_AUTHENTICATE));


        exception = new OAuth1Exception(Response.Status.INTERNAL_SERVER_ERROR, "testAuth");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("testAuth", exception.getWwwAuthHeader());
        assertEquals("testAuth", exception.getResponse().getHeaderString(HttpHeaders.WWW_AUTHENTICATE));
    }
}
