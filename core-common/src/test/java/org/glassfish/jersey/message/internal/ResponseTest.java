/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.message.internal;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 *
 * @author Paul Sandoz
 */
public class ResponseTest {

    @Test
    public void testDeclaredStatusCodes() {
        for (Status s : Status.values()) {
            StatusType _s = Response.status(s.getStatusCode()).build().getStatusInfo();
            assertSame(s, _s);
        }
    }

    @Test
    public void testUndeclaredStatusCodes() {
        StatusType st = Response.status(199).build().getStatusInfo();
        assertNotNull(st);
        assertEquals(199, st.getStatusCode());
        assertEquals("", st.getReasonPhrase());
        assertEquals(Family.INFORMATIONAL, st.getFamily());

        st = Response.status(299).build().getStatusInfo();
        assertNotNull(st);
        assertEquals(299, st.getStatusCode());
        assertEquals("", st.getReasonPhrase());
        assertEquals(Family.SUCCESSFUL, st.getFamily());

        st = Response.status(399).build().getStatusInfo();
        assertNotNull(st);
        assertEquals(399, st.getStatusCode());
        assertEquals("", st.getReasonPhrase());
        assertEquals(Family.REDIRECTION, st.getFamily());

        st = Response.status(499).build().getStatusInfo();
        assertNotNull(st);
        assertEquals(499, st.getStatusCode());
        assertEquals("", st.getReasonPhrase());
        assertEquals(Family.CLIENT_ERROR, st.getFamily());

        st = Response.status(599).build().getStatusInfo();
        assertNotNull(st);
        assertEquals(599, st.getStatusCode());
        assertEquals("", st.getReasonPhrase());
        assertEquals(Family.SERVER_ERROR, st.getFamily());
    }

    @Test
    public void reasonPhraseTest() {
        Response response = Response.status(123, "test").build();

        assertNotNull(response);
        assertEquals(123, response.getStatus());
        assertEquals("test", response.getStatusInfo().getReasonPhrase());
    }
}
