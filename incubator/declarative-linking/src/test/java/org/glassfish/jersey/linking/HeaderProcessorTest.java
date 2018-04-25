/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.glassfish.jersey.linking.InjectLink.Extension;
import org.glassfish.jersey.linking.mapping.ResourceMappingContext;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.RuntimeResource;
import org.glassfish.jersey.uri.UriTemplate;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
public class HeaderProcessorTest {

    ExtendedUriInfo mockUriInfo = new ExtendedUriInfo() {

        private static final String baseURI = "http://example.com/application/resources";

        public String getPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getPath(boolean decode) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public List<PathSegment> getPathSegments() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public List<PathSegment> getPathSegments(boolean decode) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public URI getRequestUri() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public UriBuilder getRequestUriBuilder() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public URI getAbsolutePath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public UriBuilder getAbsolutePathBuilder() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public URI getBaseUri() {
            return getBaseUriBuilder().build();
        }

        public UriBuilder getBaseUriBuilder() {
            return UriBuilder.fromUri(baseURI);
        }

        public MultivaluedMap<String, String> getPathParameters() {
            return new MultivaluedStringMap();
        }

        public MultivaluedMap<String, String> getPathParameters(boolean decode) {
            return new MultivaluedStringMap();
        }

        public MultivaluedMap<String, String> getQueryParameters() {
            return new MultivaluedStringMap();
        }

        public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
            return new MultivaluedStringMap();
        }

        public List<String> getMatchedURIs() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public List<String> getMatchedURIs(boolean decode) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public List<Object> getMatchedResources() {
            Object dummyResource = new Object() {};
            return Collections.singletonList(dummyResource);
        }

        @Override
        public URI resolve(URI uri) {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public URI relativize(URI uri) {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Throwable getMappedThrowable() {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<MatchResult> getMatchedResults() {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<UriTemplate> getMatchedTemplates() {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<PathSegment> getPathSegments(String name) {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<PathSegment> getPathSegments(String name, boolean decode) {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<RuntimeResource> getMatchedRuntimeResources() {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ResourceMethod getMatchedResourceMethod() {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Resource getMatchedModelResource() {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<ResourceMethod> getMatchedResourceLocators() {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public List<Resource> getLocatorSubResources() {
            throw new UnsupportedOperationException(
                    "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    };

    ResourceMappingContext mockRmc = new ResourceMappingContext() {

        @Override
        public ResourceMappingContext.Mapping getMapping(Class<?> resource) {
            return null;
        }
    };

    @InjectLink(value = "A")
    public static class EntityA {
    }

    @Test
    public void testLiteral() {
        System.out.println("Literal");
        HeaderProcessor<EntityA> instance = new HeaderProcessor(EntityA.class);
        EntityA testClass = new EntityA();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo, mockRmc);
        assertEquals(1, headerValues.size());
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/A>", headerValue);
    }

    @InjectLink(value = "${entity.id}")
    public static class EntityB {

        public String getId() {
            return "B";
        }
    }

    @Test
    public void testEL() {
        System.out.println("EL");
        HeaderProcessor<EntityB> instance = new HeaderProcessor(EntityB.class);
        EntityB testClass = new EntityB();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo, mockRmc);
        assertEquals(1, headerValues.size());
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/B>", headerValue);
    }

    @InjectLink(value = "{id}")
    public static class EntityC {

        public String getId() {
            return "C";
        }
    }

    @Test
    public void testTemplateLiteral() {
        System.out.println("Template Literal");
        HeaderProcessor<EntityC> instance = new HeaderProcessor(EntityC.class);
        EntityC testClass = new EntityC();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo, mockRmc);
        assertEquals(1, headerValues.size());
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/C>", headerValue);
    }

    @InjectLinks({
            @InjectLink(value = "A"),
            @InjectLink(value = "B")
    })
    public static class EntityD {
    }

    @Test
    public void testMultiple() {
        System.out.println("Multiple Literal");
        HeaderProcessor<EntityD> instance = new HeaderProcessor(EntityD.class);
        EntityD testClass = new EntityD();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo, mockRmc);
        assertEquals(2, headerValues.size());
        // not sure if annotation order is supposed to be preserved but it seems
        // to work as expected
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/A>", headerValue);
        headerValue = headerValues.get(1);
        assertEquals("</application/resources/B>", headerValue);
    }

    @InjectLink(value = "E",
            rel = "relE",
            rev = "revE",
            type = "type/e",
            title = "titleE",
            anchor = "anchorE",
            media = "mediaE",
            hreflang = "en-E",
            extensions = {
                    @Extension(name = "e1", value = "v1"),
                    @Extension(name = "e2", value = "v2")
            }
    )
    public static class EntityE {
    }

    @Test
    public void testParameters() {
        System.out.println("Parameters");
        HeaderProcessor<EntityE> instance = new HeaderProcessor(EntityE.class);
        EntityE testClass = new EntityE();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo, mockRmc);
        assertEquals(1, headerValues.size());
        String headerValue = headerValues.get(0);
        assertTrue(headerValue.contains("</application/resources/E>"));
        assertTrue(headerValue.contains("; rel=\"relE\""));
        assertTrue(headerValue.contains("; rev=\"revE\""));
        assertTrue(headerValue.contains("; type=\"type/e\""));
        assertTrue(headerValue.contains("; title=\"titleE\""));
        assertTrue(headerValue.contains("; anchor=\"anchorE\""));
        assertTrue(headerValue.contains("; media=\"mediaE\""));
        assertTrue(headerValue.contains("; hreflang=\"en-E\""));
        assertTrue(headerValue.contains("; e1=\"v1\""));
        assertTrue(headerValue.contains("; e2=\"v2\""));
    }

    @InjectLinks({
            @InjectLink(value = "${entity.id1}", condition = "${entity.id1Enabled}"),
            @InjectLink(value = "${entity.id2}", condition = "${entity.id2Enabled}")
    })
    public static class EntityF {

        public boolean isId1Enabled() {
            return true;
        }

        public String getId1() {
            return "1";
        }

        public boolean isId2Enabled() {
            return false;
        }

        public String getId2() {
            return "2";
        }
    }

    @Test
    public void testConditional() {
        System.out.println("EL");
        HeaderProcessor<EntityF> instance = new HeaderProcessor(EntityF.class);
        EntityF testClass = new EntityF();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo, mockRmc);
        assertEquals(1, headerValues.size());
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/1>", headerValue);
    }

}
