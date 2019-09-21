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

package org.glassfish.jersey.client;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Link;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * LinkTest class.
 *
 * @author Santiago.Pericas-Geertsen (santiago.pericasgeertsen at oracle.com)
 */
public class LinkTest {

    private JerseyClient client;

    public LinkTest() {
    }

    @Before
    public void setUp() {
        this.client = (JerseyClient) ClientBuilder.newClient();
    }

    @Test
    public void testInvocationFromLinkNoEntity() {
        Link l = Link.fromUri("http://examples.org/app").type("text/plain").build();
        assertNotNull(l);

        javax.ws.rs.client.Invocation i = client.invocation(l).buildGet();
        assertNotNull(i);
    }

    @Test
    public void testInvocationFromLinkWithEntity() {
        Link l = Link.fromUri("http://examples.org/app").type("*/*").build();
        Entity<String> e = Entity.text("hello world");
        javax.ws.rs.client.Invocation i = client.invocation(l).buildPost(e);
        assertTrue(i != null);
    }
}
