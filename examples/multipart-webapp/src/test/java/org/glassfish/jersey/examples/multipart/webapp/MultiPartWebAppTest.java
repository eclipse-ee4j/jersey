/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.multipart.webapp;

import java.io.File;
import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.util.SaxHelper;
import org.glassfish.jersey.internal.util.SimpleNamespaceResolver;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import org.w3c.dom.Document;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@code MultipartResource} class.
 *
 * @author Naresh (srinivas.bhimisetty at oracle.com)
 * @author Michal Gajdos
 */
public class MultiPartWebAppTest extends JerseyTest {

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path("multipart-webapp").build();
    }

    @Override
    protected Application configure() {
        return new MyApplication();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class);
    }

    @Test
    public void testApplicationWadl() throws Exception {
        final WebTarget target = target().path("application.wadl");

        final Response response = target.request().get();
        assertEquals(200, response.getStatus());
        final File tmpFile = response.readEntity(File.class);

        final DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);

        if (!SaxHelper.isXdkDocumentBuilderFactory(bf)) {
            bf.setXIncludeAware(false);
        }

        final DocumentBuilder b = bf.newDocumentBuilder();
        final Document d = b.parse(tmpFile);

        final XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new SimpleNamespaceResolver("wadl", "http://wadl.dev.java.net/2009/02"));
        String val = (String) xp.evaluate(
                "//wadl:resource[@path='part']/wadl:method[@name='POST']/wadl:request/wadl:representation/@mediaType",
                d, XPathConstants.STRING);

        assertEquals("multipart/form-data", val);
    }

    @Test
    public void testPart() {
        final WebTarget target = target().path("form/part");

        final FormDataMultiPart mp = new FormDataMultiPart();
        final FormDataBodyPart p = new FormDataBodyPart(FormDataContentDisposition.name("part").build(), "CONTENT");
        mp.bodyPart(p);

        final String s = target.request().post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA_TYPE), String.class);
        assertEquals("CONTENT", s);
    }

    @Test
    public void testPartWithFileName() {
        final WebTarget target = target().path("form/part-file-name");

        final FormDataMultiPart mp = new FormDataMultiPart();
        final FormDataBodyPart p = new FormDataBodyPart(FormDataContentDisposition.name("part").fileName("file").build(),
                "CONTENT");
        mp.bodyPart(p);

        final String s = target.request().post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA_TYPE), String.class);
        assertEquals("CONTENT:file", s);
    }

    @Test
    public void testXmlJAXBPart() {
        final WebTarget target = target().path("form/xml-jaxb-part");

        final FormDataMultiPart mp = new FormDataMultiPart();
        mp.bodyPart(new FormDataBodyPart(FormDataContentDisposition.name("bean").fileName("bean").build(),
                new Bean("BEAN"),
                MediaType.APPLICATION_XML_TYPE));
        mp.bodyPart(new FormDataBodyPart(FormDataContentDisposition.name("string").fileName("string").build(),
                "STRING"));

        final String s = target.request().post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA_TYPE), String.class);
        assertEquals("STRING:string,BEAN:bean", s);
    }

    @Test
    public void testFieldInjectedXmlJAXBPart() {
        final WebTarget target = target().path("form-field-injected/xml-jaxb-part");

        final FormDataMultiPart mp = new FormDataMultiPart();
        mp.bodyPart(new FormDataBodyPart(FormDataContentDisposition.name("bean").fileName("bean").build(),
                new Bean("BEAN"),
                MediaType.APPLICATION_XML_TYPE));
        mp.bodyPart(new FormDataBodyPart(FormDataContentDisposition.name("string").fileName("string").build(),
                "STRING"));

        final String s = target.request().post(Entity.entity(mp, MediaType.MULTIPART_FORM_DATA_TYPE), String.class);
        assertEquals("STRING:string,BEAN:bean", s);
    }
}
