/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.multipart.webapp;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.util.SaxHelper;
import org.glassfish.jersey.internal.util.SimpleNamespaceResolver;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@code MultipartResource} class.
 *
 * @author Naresh (Srinivas Bhimisetty)
 * @author Michal Gajdos
 */
public class MultiPartWebAppTest extends JerseyTest {

    public static final String PATH = MyApplication.class.getAnnotation(ApplicationPath.class).value();

    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri(super.getBaseUri()).path(PATH).build();
    }

    @Override
    protected Application configure() {
        return new MyApplication();
    }

    @Test
    public void testApplicationWadl() throws Exception {
        final WebTarget target = target().path(PATH + "/application.wadl");

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
    public void testPart() throws IOException {
        final WebTarget target = target().path(PATH + "/form/part");

        final EntityPart entityPart = EntityPart.withName("part").content("CONTENT", String.class).build();
        final GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(List.of(entityPart)) {};

        final String s = target.request().post(Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE), String.class);
        assertEquals("CONTENT", s);
    }

    @Test
    public void testPartWithFileName() throws IOException {
        final WebTarget target = target().path(PATH + "/form/part-file-name");

        final EntityPart entityPart = EntityPart.withName("part").fileName("file").content("CONTENT", String.class).build();
        final GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(List.of(entityPart)) {};

        final String s = target.request().post(Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE), String.class);
        assertEquals("CONTENT:file", s);
    }

    @Test
    public void testXmlJAXBPart() throws IOException {
        final WebTarget target = target().path(PATH + "/form/xml-jaxb-part");

        final EntityPart entityPart1 = EntityPart.withName("bean").fileName("bean")
                .content(new Bean("BEAN"), Bean.class)
                .mediaType(MediaType.APPLICATION_XML_TYPE)
                .build();
        final EntityPart entityPart2 = EntityPart.withName("string").fileName("string")
                .content("STRING", String.class)
                .build();

        final GenericEntity<List<EntityPart>> genericEntity = new GenericEntity<>(List.of(entityPart1, entityPart2)) {};

        final String s = target.request().post(Entity.entity(genericEntity, MediaType.MULTIPART_FORM_DATA_TYPE), String.class);
        assertEquals("STRING:string,BEAN:bean", s);
    }
}
