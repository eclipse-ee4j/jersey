/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test of {@link javax.xml.transform.Source Source} MessageBody Provider
 *
 * @author Miroslav Fuksa
 *
 */
public class SourceEntityProviderTest extends JerseyTest {

    private static final String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"";
    private static final String xdkPrefix = "<?xml version = '1.0' encoding = 'UTF-8'?>";
    private static final String entity = prefix + "?><test><aaa/></test>";

    @Override
    protected ResourceConfig configure() {
        return new ResourceConfig(TestResource.class);
    }

    private static String extractContent(Source source) throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException {
        TransformerFactory transFactory = TransformerFactory.newInstance();

        // identity transformation
        Transformer transformer = transFactory.newTransformer();

        StringWriter writer = new StringWriter();
        Result result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }

    @Test
    public void sourceProviderTest() throws IOException, TransformerConfigurationException, TransformerFactoryConfigurationError,
            TransformerException {
        Source source = new StreamSource(new ByteArrayInputStream(entity.getBytes()));

        Response response = target().path("test").path("source").request().put(Entity.entity(source, MediaType.TEXT_XML_TYPE));
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.readEntity(String.class).startsWith(StreamSource.class.toString()));
    }

    @Test
    public void streamProviderTest() throws IOException {
        StreamSource source = new StreamSource(new ByteArrayInputStream(entity.getBytes()));

        Response response = target().path("test").path("stream").request().put(Entity.entity(source, MediaType.TEXT_XML_TYPE));
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.readEntity(String.class).startsWith(StreamSource.class.toString()));
    }

    @Test
    public void saxProviderTest() throws IOException, SAXException, ParserConfigurationException {
        SAXSource source = createSAXSource(entity);

        Response response = target().path("test").path("sax").request().put(Entity.entity(source, MediaType.TEXT_XML_TYPE));
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.readEntity(String.class).startsWith(SAXSource.class.toString()));
    }

    @Test
    public void domProviderTest() throws IOException, SAXException, ParserConfigurationException {
        DOMSource source = createDOMSoruce(entity);

        Response response = target().path("test").path("dom").request().put(Entity.entity(source, MediaType.TEXT_XML_TYPE));
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.readEntity(String.class).startsWith(DOMSource.class.toString()));
    }

    @Test
    public void getSourceTest() throws Exception {
        Response response = target().path("test").path("source").request().get();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        String content = extractContent(response.readEntity(Source.class));
        assertTrue(content.startsWith(prefix) || content.startsWith(xdkPrefix));
    }

    @Test
    public void getStreamSourceTest() throws Exception {
        Response response = target().path("test").path("stream").request().get();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        String content = extractContent(response.readEntity(StreamSource.class));
        assertTrue(content.startsWith(prefix) || content.startsWith(xdkPrefix));
    }

    @Test
    public void getSaxSourceTest() throws Exception {
        Response response = target().path("test").path("sax").request().get();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        String content = extractContent(response.readEntity(SAXSource.class));
        assertTrue("Content '" + content + "' does not start with the expected prefix '" + prefix + "'",
                content.startsWith(prefix) || content.startsWith(xdkPrefix));
    }

    @Test
    public void getDomSourceTest() throws Exception {
        Response response = target().path("test").path("dom").request().get();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        String content = extractContent(response.readEntity(DOMSource.class));
        assertTrue("Content '" + content + "' does not start with the expected prefix '" + prefix + "'",
                content.startsWith(prefix) || content.startsWith(xdkPrefix));
    }

    private static SAXSource createSAXSource(String content) throws SAXException, ParserConfigurationException {
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        return new SAXSource(saxFactory.newSAXParser().getXMLReader(), new InputSource(new ByteArrayInputStream(
                content.getBytes())));
    }

    private static DOMSource createDOMSoruce(String content) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document d = documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(content.getBytes()));
        return new DOMSource(d);
    }

    @Path("test")
    public static class TestResource {

        @PUT
        @Consumes("text/xml")
        @Path("source")
        public String putSourceAndReturnString(Source source) throws IOException, TransformerException {
            return source.getClass() + extractContent(source);
        }

        @PUT
        @Consumes("text/xml")
        @Path("stream")
        public String putStreamSourceAndReturnString(StreamSource source) throws IOException, TransformerException {
            return source.getClass() + extractContent(source);
        }

        @PUT
        @Consumes("text/xml")
        @Path("sax")
        public String putSaxSourceAndReturnString(SAXSource source) throws IOException, TransformerException {
            return source.getClass() + extractContent(source);
        }

        @PUT
        @Consumes("text/xml")
        @Path("dom")
        public String putDomSourceAndReturnString(DOMSource source) throws IOException, TransformerException {
            return source.getClass() + extractContent(source);
        }

        @GET
        @Produces("application/xml")
        @Path("source")
        public StreamSource getSource() {
            return new StreamSource(new ByteArrayInputStream(entity.getBytes()));
        }

        @GET
        @Produces("application/xml")
        @Path("stream")
        public StreamSource getStreamSource() {
            return new StreamSource(new ByteArrayInputStream(entity.getBytes()));
        }

        @GET
        @Produces("application/xml")
        @Path("sax")
        public SAXSource getSaxSource() throws SAXException, ParserConfigurationException {
            return createSAXSource(entity);
        }

        @GET
        @Produces("application/xml")
        @Path("dom")
        public DOMSource getDomSource() throws Exception {
            return createDOMSoruce(entity);
        }
    }
}
