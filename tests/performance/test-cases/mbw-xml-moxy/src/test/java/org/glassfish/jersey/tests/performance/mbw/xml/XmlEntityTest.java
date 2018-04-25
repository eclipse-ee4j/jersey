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

package org.glassfish.jersey.tests.performance.mbw.xml;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.test.JerseyTest;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test for XML resource.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class XmlEntityTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new JaxRsApplication();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MoxyXmlFeature.class);
    }

    @Test
    public void testGet() {
        final Person getResponse = target().request().get(Person.class);
        assertEquals("Wolfgang", getResponse.name);
        assertEquals(21, getResponse.age);
        assertEquals("Salzburg", getResponse.address);
    }

    @Test
    public void testPost() {
        final Person[] testData = new Person[] {new Person("Joseph", 23, "Nazareth"), new Person("Mary", 18, "Nazareth") };
        for (Person original : testData) {
            final Person postResponse = target().request().post(Entity.xml(original), Person.class);
            assertEquals(original, postResponse);
        }
    }

    @Test
    public void testPut() {
        final Response putResponse = target().request().put(Entity.xml(new Person("Jules", 12, "Paris")));
        assertEquals(204, putResponse.getStatus());
    }
}
