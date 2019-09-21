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

package org.glassfish.jersey.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author pavel.bucek@oracle.com
 */
public class WebTargetPropertiesTest {

    @Test
    public void testPropagation() {
        Client c = ClientBuilder.newBuilder().newClient();

        c.property("a", "val");

        WebTarget w1 = c.target("http://a");
        w1.property("b", "val");

        WebTarget w2 = w1.path("c");
        w2.property("c", "val");

        assertTrue(c.getConfiguration().getProperties().containsKey("a"));
        assertTrue(w1.getConfiguration().getProperties().containsKey("a"));
        assertTrue(w2.getConfiguration().getProperties().containsKey("a"));

        assertFalse(c.getConfiguration().getProperties().containsKey("b"));
        assertTrue(w1.getConfiguration().getProperties().containsKey("b"));
        assertTrue(w2.getConfiguration().getProperties().containsKey("b"));

        assertFalse(c.getConfiguration().getProperties().containsKey("c"));
        assertFalse(w1.getConfiguration().getProperties().containsKey("c"));
        assertTrue(w2.getConfiguration().getProperties().containsKey("c"));

        w2.property("a", null);

        assertTrue(c.getConfiguration().getProperties().containsKey("a"));
        assertTrue(w1.getConfiguration().getProperties().containsKey("a"));
        assertFalse(w2.getConfiguration().getProperties().containsKey("a"));
    }
}
