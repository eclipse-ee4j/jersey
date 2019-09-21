/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.common.message.internal;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.message.internal.OutboundJaxrsResponse;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.tests.e2e.common.TestRuntimeDelegate;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * JaxrsResponseViewTest class.
 *
 * @author Santiago Pericas-Geertsen (santiago.pericasgeertsen at oracle.com)
 */
public class OutboundJaxrsResponseBuilderTest {

    /**
     * Create test class.
     */
    public OutboundJaxrsResponseBuilderTest() {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    /**
     * Test media type header setting, retrieval.
     */
    @Test
    public void testMediaType() {
        final Response r = new OutboundJaxrsResponse.Builder(new OutboundMessageContext())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML)
                .build();
        assertEquals(204, r.getStatus());
        assertEquals(Response.Status.NO_CONTENT, r.getStatusInfo());
        assertEquals(MediaType.TEXT_HTML_TYPE, r.getMediaType());
    }

    @Test
    public void testIssue1297Fix() {
        final Response response = new OutboundJaxrsResponse.Builder(new OutboundMessageContext())
                .status(Response.Status.OK)
                .entity("1234567890")
                .build();
        final int len = response.getLength();
        assertEquals(-1, len);
    }
}
