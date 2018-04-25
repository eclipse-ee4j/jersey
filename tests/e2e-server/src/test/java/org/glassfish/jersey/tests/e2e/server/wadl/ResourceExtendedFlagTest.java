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

package org.glassfish.jersey.tests.e2e.server.wadl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.jersey.internal.util.SaxHelper;
import org.glassfish.jersey.internal.util.SimpleNamespaceResolver;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ExtendedResourceContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.ExtendedResource;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.wadl.internal.WadlUtils;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import static org.junit.Assert.assertEquals;

/**
 * Test verifies functionality of {@link org.glassfish.jersey.server.model.Resource#isExtended()} and its
 * influence on WADL.
 *
 * @author Miroslav Fuksa
 */
public class ResourceExtendedFlagTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(MyResource.class, AllExtended.class, LoggingFeature.class);
    }

    @Path("all-extended")
    public static class AllExtended {

        @GET
        @ExtendedResource
        public String allExtendedGet() {
            return "get";
        }

        @GET
        @Path("sub")
        @ExtendedResource
        public String allExtendedSubGet() {
            return "sub-get";
        }

        @Path("locator")
        @ExtendedResource
        public SubResourceLocator allExtendedSubLocator() {
            return new SubResourceLocator();
        }
    }

    public static class SubResourceLocator {

        @GET
        public String get() {
            return "get";
        }
    }

    @Path("resource")
    public static class MyResource {

        @Context
        private ExtendedResourceContext extendedResourceContext;

        @GET
        public String resourceGet() {

            final String error = validateModel();
            return error == null ? "ok" : error;
        }

        @GET
        @Path("extended")
        @ExtendedResource
        public String resourceExtendedGet() {
            return "extended";
        }

        @POST
        @ExtendedResource
        public String resourceExtendedPost() {
            return "extendedpost";
        }

        @POST
        @Path("visible")
        public String resourceVisiblePost() {
            return "visiblepost";
        }

        private String validateModel() {
            Map<String, Boolean> extendedMethods = new HashMap<>();
            extendedMethods.put("resourceExtendedGet", false);
            extendedMethods.put("resourceExtendedPost", false);
            extendedMethods.put("allExtendedGet", false);
            extendedMethods.put("allExtendedSubGet", false);
            extendedMethods.put("allExtendedSubLocator", false);

            Map<String, Boolean> visibleMethods = new HashMap<>();
            visibleMethods.put("resourceVisiblePost", false);
            visibleMethods.put("resourceGet", false);

            final ResourceModel resourceModel = extendedResourceContext.getResourceModel();
            for (Resource rootResource : resourceModel.getRootResources()) {
                final String error = checkResource(rootResource, extendedMethods, visibleMethods, "");
                if (error != null) {
                    return error;
                }
            }

            for (Map.Entry<String, Boolean> entry : extendedMethods.entrySet()) {
                if (entry.getValue() != null && !entry.getValue()) {
                    return "Extended method " + entry.getKey() + " was not found";
                }
            }

            for (Map.Entry<String, Boolean> entry : visibleMethods.entrySet()) {
                if (entry.getValue() != null && !entry.getValue()) {
                    return "Visible method " + entry.getKey() + " was not found";
                }
            }
            return null;
        }

        private String checkResource(Resource resource, Map<String, Boolean> extendedMethods,
                                     Map<String, Boolean> visibleMethods, String prefix) {

            System.out.println(prefix + "R extended=" + resource.isExtended() + " resource: " + resource);

            boolean allExtended = true;
            for (ResourceMethod resourceMethod : resource.getAllMethods()) {
                if ("OPTIONS".equals(resourceMethod.getHttpMethod()) && !resourceMethod.isExtended()) {
                    return "OPTIONS method " + resourceMethod + " is not extended";
                }
                if (!resourceMethod.isExtended()) {
                    allExtended = false;
                }

                final String methodName = resourceMethod.getInvocable().getHandlingMethod().getName();
                if (extendedMethods.get(methodName) != null) {
                    extendedMethods.put(methodName, true);
                    if (!resourceMethod.isExtended()) {
                        return "Method " + methodName + " should be extended.";
                    }
                }

                if (visibleMethods.get(methodName) != null) {
                    visibleMethods.put(methodName, true);
                    if (resourceMethod.isExtended()) {
                        return "Method " + methodName + " should NOT be extended.";
                    }
                }
                System.out.println(prefix + "   M extended=" + resourceMethod.isExtended() + " method: " + methodName);
            }

            for (Resource child : resource.getChildResources()) {
                final String error = checkResource(child, extendedMethods, visibleMethods, "     ");
                if (error != null) {
                    return error;
                }
            }

            if (allExtended != resource.isExtended()) {
                return "Resource " + resource + "Resource.extended = " + resource.isExtended() + " and allExtended="
                        + allExtended;
            }
            return null;
        }
    }

    /**
     * Invokes resource method which goes trough the {@link org.glassfish.jersey.server.model.ResourceModel}
     * and checks whether {@code extended} flag is correctly defined on resources and methods.
     */
    @Test
    public void testResourceModel() {
        final Response response = target().path("resource").request().get();
        assertEquals(200, response.getStatus());
        assertEquals("ok", response.readEntity(String.class));
    }

    /**
     * Tests full wadl with all "extended" details.
     *
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void testDetailedWadl() throws ParserConfigurationException, XPathExpressionException, IOException,
            SAXException {
        Response response = target("/application.wadl").queryParam(WadlUtils.DETAILED_WADL_QUERY_PARAM, "true")
                .request(MediaTypes.WADL_TYPE).get();
        assertEquals(200, response.getStatus());
        File tmpFile = response.readEntity(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        if (!SaxHelper.isXdkDocumentBuilderFactory(bf)) {
            bf.setXIncludeAware(false);
        }
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new SimpleNamespaceResolver("wadl", "http://wadl.dev.java.net/2009/02"));

        // check base URI
        String val = (String) xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val, getBaseUri().toString());
        // check total number of resources is 8
        val = (String) xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals("8", val);

        val = (String) xp.evaluate("count(//wadl:resource[@path='all-extended'])", d, XPathConstants.STRING);
        assertEquals("1", val);

        val = (String) xp.evaluate("count(//wadl:resource[@path='resource'])", d, XPathConstants.STRING);
        assertEquals("1", val);

        val = (String) xp.evaluate("count(//wadl:resource[@path='application.wadl'])", d, XPathConstants.STRING);
        assertEquals("1", val);

        xp.setNamespaceContext(new SimpleNamespaceResolver("jersey", "http://jersey.java.net/"));
        val = (String) xp.evaluate("count(//jersey:extended)", d, XPathConstants.STRING);
        assertEquals("31", val);
    }

    /**
     * Tests limited wadl with only user resources.
     *
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void testLimitedWadl() throws ParserConfigurationException, XPathExpressionException, IOException,
            SAXException {
        Response response = target("/application.wadl").request(MediaTypes.WADL_TYPE).get();
        assertEquals(200, response.getStatus());
        File tmpFile = response.readEntity(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        if (!SaxHelper.isXdkDocumentBuilderFactory(bf)) {
            bf.setXIncludeAware(false);
        }
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new SimpleNamespaceResolver("wadl", "http://wadl.dev.java.net/2009/02"));

        // check base URI
        String val = (String) xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val, getBaseUri().toString());
        // check total number of resources is 8
        val = (String) xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals("2", val);

        val = (String) xp.evaluate("count(//wadl:resource[@path='all-extended'])", d, XPathConstants.STRING);
        assertEquals("0", val);

        val = (String) xp.evaluate("count(//wadl:resource[@path='resource'])", d, XPathConstants.STRING);
        assertEquals("1", val);

        val = (String) xp.evaluate("count(//wadl:resource[@path='application.wadl'])", d, XPathConstants.STRING);
        assertEquals("0", val);

        xp.setNamespaceContext(new SimpleNamespaceResolver("jersey", "http://jersey.java.net/"));
        val = (String) xp.evaluate("count(//jersey:extended)", d, XPathConstants.STRING);
        assertEquals("0", val);
    }

    public static void printSource(Source source) {
        try {
            System.out.println("---------------------");
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            oprops.put(OutputKeys.INDENT, "yes");
            oprops.put(OutputKeys.METHOD, "xml");
            trans.setOutputProperties(oprops);
            trans.transform(source, new StreamResult(System.out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
