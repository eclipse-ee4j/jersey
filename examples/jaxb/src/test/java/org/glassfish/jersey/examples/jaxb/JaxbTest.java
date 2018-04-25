/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.jaxb;

import java.util.Collection;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import static javax.ws.rs.client.Entity.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Jersey JAXB example test.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class JaxbTest extends JerseyTest {

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return App.createApp();
    }

    /**
     * Test checks that the application.wadl is reachable.
     */
    @Test
    public void testApplicationWadl() {
        String applicationWadl = target().path("application.wadl").request().get(String.class);
        assertTrue("Something wrong. Returned wadl length is not > 0",
                applicationWadl.length() > 0);
    }

    @Test
    public void testRootElement() {
        JaxbXmlRootElement e1 = target().path("jaxb/XmlRootElement").request().get(JaxbXmlRootElement.class);

        JaxbXmlRootElement e2 = target().path("jaxb/XmlRootElement").request("application/xml")
                .post(xml(e1), JaxbXmlRootElement.class);

        assertEquals(e1, e2);
    }

    @Test
    public void testRootElementWithHeader() {
        String e1 = target().path("jaxb/XmlRootElement").request().get(String.class);

        String e2 = target().path("jaxb/XmlRootElementWithHeader").request().get(String.class);
        assertTrue(e2.contains("<?xml-stylesheet type='text/xsl' href='foobar.xsl' ?>") && e2.contains(e1.substring(e1.indexOf("?>") + 2).trim()));
    }

    @Test
    public void testJAXBElement() {
        GenericType<JAXBElement<JaxbXmlType>> genericType = new GenericType<JAXBElement<JaxbXmlType>>() {};

        JAXBElement<JaxbXmlType> e1 = target().path("jaxb/JAXBElement").request().get(genericType);

        JAXBElement<JaxbXmlType> e2 = target().path("jaxb/JAXBElement").request("application/xml")
                .post(xml(e1), genericType);

        assertEquals(e1.getValue(), e2.getValue());
    }

    @Test
    public void testXmlType() {
        JaxbXmlType t1 = target().path("jaxb/JAXBElement").request().get(JaxbXmlType.class);

        JAXBElement<JaxbXmlType> e = new JAXBElement<JaxbXmlType>(
                new QName("jaxbXmlRootElement"),
                JaxbXmlType.class,
                t1);
        JaxbXmlType t2 = target().path("jaxb/XmlType").request("application/xml")
                .post(xml(e), JaxbXmlType.class);

        assertEquals(t1, t2);
    }


    @Test
    public void testRootElementCollection() {
        GenericType<Collection<JaxbXmlRootElement>> genericType =
                new GenericType<Collection<JaxbXmlRootElement>>() {};

        Collection<JaxbXmlRootElement> ce1 = target().path("jaxb/collection/XmlRootElement").request().get(genericType);
        Collection<JaxbXmlRootElement> ce2 = target().path("jaxb/collection/XmlRootElement").request("application/xml")
                .post(xml(new GenericEntity<Collection<JaxbXmlRootElement>>(ce1) {}), genericType);

        assertEquals(ce1, ce2);
    }

    @Test
    public void testXmlTypeCollection() {
        GenericType<Collection<JaxbXmlRootElement>> genericRootElement =
                new GenericType<Collection<JaxbXmlRootElement>>() {};
        GenericType<Collection<JaxbXmlType>> genericXmlType =
                new GenericType<Collection<JaxbXmlType>>() {
                };

        Collection<JaxbXmlRootElement> ce1 = target().path("jaxb/collection/XmlRootElement").request()
                .get(genericRootElement);

        Collection<JaxbXmlType> ct1 = target().path("jaxb/collection/XmlType").request("application/xml")
                .post(xml(new GenericEntity<Collection<JaxbXmlRootElement>>(ce1) {}), genericXmlType);

        Collection<JaxbXmlType> ct2 = target().path("jaxb/collection/XmlRootElement").request()
                .get(genericXmlType);

        assertEquals(ct1, ct2);
    }

    @Test
    public void testRootElementArray() {
        JaxbXmlRootElement[] ae1 = target().path("jaxb/array/XmlRootElement").request()
                .get(JaxbXmlRootElement[].class);
        JaxbXmlRootElement[] ae2 = target().path("jaxb/array/XmlRootElement").request("application/xml")
                .post(xml(ae1), JaxbXmlRootElement[].class);

        assertEquals(ae1.length, ae2.length);
        for (int i = 0; i < ae1.length; i++) {
            assertEquals(ae1[i], ae2[i]);
        }
    }

    @Test
    public void testXmlTypeArray() {
        JaxbXmlRootElement[] ae1 = target().path("jaxb/array/XmlRootElement").request()
                .get(JaxbXmlRootElement[].class);

        JaxbXmlType[] at1 = target().path("jaxb/array/XmlType").request("application/xml")
                .post(xml(ae1), JaxbXmlType[].class);

        JaxbXmlType[] at2 = target().path("jaxb/array/XmlRootElement").request()
                .get(JaxbXmlType[].class);

        assertEquals(at1.length, at2.length);
        for (int i = 0; i < at1.length; i++) {
            assertEquals(at1[i], at2[i]);
        }
    }
}
