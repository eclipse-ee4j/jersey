/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.externalproperties.jaxb;

import org.glassfish.jersey.ExternalProperties;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Properties;

public class JaxbPropertiesTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig()
                .packages(JaxbResource.class.getPackage().getName());
    }

    @Test
    public void testExpectedBehavior() {
        _test();
    }

    @Test
    public void testJAXBContextFactory() {
        System.setProperty(ExternalProperties.JAXB_CONTEXT_FACTORY, "wrong.factory");

        try {
            _test();
        } catch (Exception e) {
            Assert.assertEquals("wrong.factory", e.getCause().getCause().getMessage());
        }

    }

    @Test
    public void testJAXBContext() {
        System.setProperty(ExternalProperties.JAXB_CONTEXT,  "wrong.factory");

        try {
            _test();
        } catch (Exception e) {
            Assert.assertEquals("wrong.factory", e.getCause().getCause().getMessage());
        }
    }

    @Test
    public void testContextFactory() {
        System.setProperty(ExternalProperties.CONTEXT_FACTORY, "wrong.factory");

        try {
            _test();
        } catch (Exception e) {
            Assert.assertEquals("wrong.factory", e.getCause().getCause().getMessage());
        }
    }

    private void _test() {
        Book book = new Book(
                "Harry Potter and the Chamber of Secrets",
                "J. K. Rowling",
                120,
                500
        );

        Response response = target("library").request().get();

        Assert.assertEquals("Welcome to the Library", response.readEntity(String.class));

        response = target("library").request(MediaType.APPLICATION_XML)
                .post(Entity.entity(book, MediaType.APPLICATION_XML));

        Assert.assertEquals("Book added to the Library", response.readEntity(String.class));

        response = target("/library/Harry%20Potter%20and%20the%20Chamber%20of%20Secrets")
                .request().get();

        Assert.assertEquals(book, response.readEntity(Book.class));

        response = target("/library/Harry%20Potter%20and%20the%20Chamber%20of%20Secrets")
                .request().delete();

        Assert.assertEquals("Harry Potter and the Chamber of Secrets successfully removed from library",
                response.readEntity(String.class));

        try {
            target("/library/Wrong%20Book").request().get();
        } catch (Exception e) {
            Assert.assertEquals("Error: This book is not at the library", e.getMessage());
        }

    }

    @Before
    public void removeSystemProperties() {
        Properties properties =  System.getProperties();
        properties.remove(ExternalProperties.JAXB_CONTEXT_FACTORY);
        properties.remove(ExternalProperties.JAXB_CONTEXT);
        properties.remove(ExternalProperties.CONTEXT_FACTORY);
    }

}
