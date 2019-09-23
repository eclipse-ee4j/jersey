/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.filter.name;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test for intercepted text plain resource.
 *
 * @author Jakub Podlesak
 */
public class FilterTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new JaxRsApplication();
    }

    @Test
    public void testGet() {
        final String getResponse = target().request().get(String.class);
        assertEquals("textNAM_MATCH_OUT", getResponse);
    }

    @Test
    public void testPost() {
        final String[] testData = new String[] {"one", "two", "three" };
        for (String original : testData) {
            final String postResponse = target().request().post(Entity.text(original), String.class);
            assertEquals("NAM_MATCH_IN" + original + "NAM_MATCH_OUT", postResponse);
        }
    }

    @Test
    public void testPut() {
        final Response putResponse = target().request().put(Entity.text("text"));
        assertEquals(204, putResponse.getStatus());
    }
}
