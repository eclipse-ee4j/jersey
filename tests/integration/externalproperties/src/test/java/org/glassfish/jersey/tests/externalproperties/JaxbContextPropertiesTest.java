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

package org.glassfish.jersey.tests.externalproperties;

import org.glassfish.jersey.ExternalProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.jdk.connector.JdkConnectorProvider;
import org.glassfish.jersey.jetty.connector.JettyConnectorProvider;
import org.glassfish.jersey.netty.connector.NettyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@RunWith(Parameterized.class)
public class JaxbContextPropertiesTest extends JerseyTest {

    private final ConnectorProvider connectorProvider;

    @Parameterized.Parameters(name = "{index}: {0}")
    public static List<Object[]> connectors() {
        return Arrays.asList(new Object[][]{
                {HttpUrlConnectorProvider.class},
                {GrizzlyConnectorProvider.class},
                {JettyConnectorProvider.class},
                {ApacheConnectorProvider.class},
                {GrizzlyConnectorProvider.class},
                {NettyConnectorProvider.class},
                {JdkConnectorProvider.class},
        });
    }

    public JaxbContextPropertiesTest(Class<? extends ConnectorProvider> connectorProviderClass)
            throws IllegalAccessException, InstantiationException {
        this.connectorProvider = connectorProviderClass.newInstance();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "Book")
    static class Book {
        @XmlElement
        private String title;

        public Book() {
        }

        public Book(String title) {
            setTitle(title);
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    @Path("resource")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public static class MyResource {

        @POST
        @Path("getBook")
        @Produces(MediaType.APPLICATION_XML)
        @Consumes(MediaType.APPLICATION_XML)
        public Book getBook(Book book) {
            return book;
        }
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class);
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.connectorProvider(connectorProvider);
    }

    @Before
    public void removeSystemProperties() {
        Properties properties =  System.getProperties();
        properties.remove(ExternalProperties.JAXB_CONTEXT_FACTORY);
        properties.remove(ExternalProperties.JAXB_CONTEXT);
        properties.remove(ExternalProperties.CONTEXT_FACTORY);
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
        } catch (InternalServerErrorException e) {
            Assert.assertEquals("wrong.factory", e.getCause().getCause().getMessage());
        }
    }

    @Test
    public void testJAXBContext() {
        System.setProperty(ExternalProperties.JAXB_CONTEXT,  "wrong.factory");

        try {
            _test();
        } catch (InternalServerErrorException e) {
            Assert.assertEquals("wrong.factory", e.getCause().getCause().getMessage());
        }
    }

    @Test
    public void testContextFactory() {
        System.setProperty(ExternalProperties.CONTEXT_FACTORY, "wrong.factory");

        try {
            _test();
        } catch (InternalServerErrorException e) {
            Assert.assertEquals("wrong.factory", e.getCause().getCause().getMessage());
        }
    }

    private void _test() {
        final String title = "Harry Potter";
        Book book = new Book(title);

        Response response = target("resource/getBook")
                .request(MediaType.APPLICATION_XML)
                .post(Entity.entity(book, MediaType.APPLICATION_XML));

        Assert.assertEquals(title, response.readEntity(Book.class).getTitle());
    }

}
