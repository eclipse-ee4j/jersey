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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * An example resource utilizing array of JAXB beans.
 *
 * @author Paul Sandoz
 */
@Path("jaxb/array")
@Produces("application/xml")
@Consumes("application/xml")
public class JaxbArrayResource {

    @Path("XmlRootElement")
    @GET
    public JaxbXmlRootElement[] getRootElement() {
        List<JaxbXmlRootElement> el = new ArrayList<JaxbXmlRootElement>();
        el.add(new JaxbXmlRootElement("one root element"));
        el.add(new JaxbXmlRootElement("two root element"));
        el.add(new JaxbXmlRootElement("three root element"));
        return el.toArray(new JaxbXmlRootElement[el.size()]);
    }

    @Path("XmlRootElement")
    @POST
    public JaxbXmlRootElement[] postRootElement(JaxbXmlRootElement[] el) {
        return el;
    }

    @Path("XmlType")
    @POST
    public JaxbXmlRootElement[] postXmlType(JaxbXmlType[] tl) {
        List<JaxbXmlRootElement> el = new ArrayList<JaxbXmlRootElement>();

        for (JaxbXmlType t : tl) {
            el.add(new JaxbXmlRootElement(t.value));
        }

        return el.toArray(new JaxbXmlRootElement[el.size()]);
    }
}
