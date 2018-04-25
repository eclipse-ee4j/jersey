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

package org.glassfish.jersey.tests.e2e.server;

import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.SAXParserFactory;

import org.glassfish.jersey.internal.util.SaxHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXParseException;

/**
 * Tests properties configuring secure sax parsing.
 *
 * @author Miroslav Fuksa
 */
public class EntityExpansionTest extends JerseyTest {

    private static final Logger LOG = Logger.getLogger(EntityExpansionTest.class.getName());
    private static boolean isXdk = false;

    @Override
    protected Application configure() {
        System.setProperty("entityExpansionLimit", "10");
        System.setProperty("elementAttributeLimit", "1");

        final ResourceConfig resourceConfig = new ResourceConfig(TestResource.class, BadRequestMapper.class);
        return resourceConfig;
    }

    public static class BadRequestMapper implements ExceptionMapper<BadRequestException> {
        @Override
        public Response toResponse(BadRequestException exception) {
            Throwable t = exception;
            while (t != null && t.getClass() != SAXParseException.class) {
                t = t.getCause();
            }
            if (t != null) {
                return Response.ok().entity("PASSED:" + t.getMessage()).build();
            }
            return Response.status(500).build();
        }
    }

    @Path("resource")
    public static class TestResource {

        @POST
        public String post(TestBean bean) {
            return bean.getInput();
        }

    }

    @XmlRootElement()
    @XmlAccessorType(value = XmlAccessType.FIELD)
    public static class TestBean {
        @XmlElement
        private String input;

        @XmlAttribute
        private String str;

        @XmlAttribute
        private String str2;

        @XmlAttribute
        private String str3;


        public String getStr2() {
            return str2;
        }

        public void setStr2(String str2) {
            this.str2 = str2;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }


        public String getStr3() {
            return str3;
        }

        public void setStr3(String str3) {
            this.str3 = str3;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }
    }

    @BeforeClass
    public static void setXdkFlag() {
        // XDK SAXParser does not support this feature, so the test has to be skipped if XDK detected.
        if (SaxHelper.isXdkParserFactory(SAXParserFactory.newInstance())) {
            LOG.warning("XDK SAXParser detected, FEATURE_SECURE_PROCESSING is not supported. Tests will be skipped.");
            isXdk = true;
        }
        Assume.assumeTrue(!isXdk);
    }

    @Test
    public void testEntityExpansion() {
        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "\n<!DOCTYPE lolz [\n"
                + "  <!ENTITY lol \"lollollollollollollol[...]\">\n"
                + "  <!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n"
                + "  <!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n"
                + "]>\n"
                + "<testBean><input>&lol3;</input></testBean>";

        final Response response = target().path("resource").request().post(Entity.entity(str, MediaType.APPLICATION_XML));
        Assert.assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        Assert.assertTrue(entity.startsWith("PASSED"));
    }

    @Test
    public void testMaxAttributes() {
        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<testBean str=\"aaa\" str2=\"bbb\" str3=\"ccc\"><input>test</input></testBean>";
        final Response response = target().path("resource").request().post(Entity.entity(str, MediaType.APPLICATION_XML));
        Assert.assertEquals(200, response.getStatus());
        final String entity = response.readEntity(String.class);
        Assert.assertTrue(entity.startsWith("PASSED"));
    }

}
