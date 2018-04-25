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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.glassfish.jersey.message.XmlHeader;

/**
 * An example resource utilizing JAXB beans.
 *
 * @author Paul Sandoz
 */
@Path("jaxb")
@Produces("application/xml")
@Consumes("application/xml")
public class JaxbResource {

    @Path("XmlRootElement")
    @GET
    public JaxbXmlRootElement getRootElement() {
        return new JaxbXmlRootElement("xml root element");
    }

    @Path("XmlRootElementWithHeader")
    @GET
    @XmlHeader("<?xml-stylesheet type='text/xsl' href='foobar.xsl' ?>")
    public JaxbXmlRootElement getRootElementWithHeader() {
        return new JaxbXmlRootElement("xml root element");
    }

    @Path("XmlRootElement")
    @POST
    public JaxbXmlRootElement postRootElement(JaxbXmlRootElement r) {
        return r;
    }

    @Path("JAXBElement")
    @GET
    public JAXBElement<JaxbXmlType> getJAXBElement() {
        return new JAXBElement<JaxbXmlType>(
                new QName("jaxbXmlRootElement"),
                JaxbXmlType.class,
                new JaxbXmlType("xml type"));
    }

    @Path("JAXBElement")
    @POST
    public JAXBElement<JaxbXmlType> postJAXBElement(JAXBElement<JaxbXmlType> e) {
        return e;
    }

    @Path("XmlType")
    @POST
    public JAXBElement<JaxbXmlType> postXmlType(JaxbXmlType r) {
        return new JAXBElement<JaxbXmlType>(
                new QName("jaxbXmlRootElement"), JaxbXmlType.class, r);
    }
}
