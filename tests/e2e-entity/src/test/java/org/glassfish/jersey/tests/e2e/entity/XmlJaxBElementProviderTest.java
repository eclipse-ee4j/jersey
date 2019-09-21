/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.entity;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Miroslav Fuksa
 *
 */
public class XmlJaxBElementProviderTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(Atom.class);
    }

    @Path("atom")
    public static class Atom {
        @Context
        HttpHeaders headers;

        @Path("wildcard")
        @POST
        @Consumes("application/*")
        @Produces("application/*")
        public Response wildcard(JAXBElement<String> jaxb) {
            MediaType media = headers.getMediaType();
            return Response.ok(jaxb).type(media).build();
        }

        @Path("atom")
        @POST
        @Consumes("application/atom+xml")
        @Produces("application/atom+xml")
        public Response atom(JAXBElement<String> jaxb) {
            MediaType media = headers.getMediaType();
            return Response.ok(jaxb).type(media).build();
        }

        @Path("empty")
        @POST
        public Response emptyConsumesProduces(JAXBElement<String> jaxb) {
            MediaType media = headers.getMediaType();
            return Response.ok(jaxb).type(media).build();
        }
    }

    @Test
    public void testWildcard() {
        final String path = "atom/wildcard";
        _test(path);
    }

    private void _test(String path) {
        WebTarget target = target(path);
        final Response res = target.request("application/atom+xml").post(
                Entity.entity(new JAXBElement<String>(new QName("atom"), String.class, "value"),
                        "application/atom+xml"));
        assertEquals(200, res.getStatus());
        final GenericType<JAXBElement<String>> genericType = new GenericType<JAXBElement<String>>() {};
        final JAXBElement<String> stringJAXBElement = res.readEntity(genericType);
        assertEquals("value", stringJAXBElement.getValue());
    }

    @Test
    public void testAtom() {
        final String path = "atom/atom";
        _test(path);
    }

    @Test
    public void testEmpty() {
        final String path = "atom/empty";
        _test(path);
    }
}
