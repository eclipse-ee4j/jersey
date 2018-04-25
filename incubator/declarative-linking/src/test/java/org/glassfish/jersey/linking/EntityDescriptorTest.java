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
import java.util.Iterator;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.linking.mapping.ResourceMappingContext;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
public class EntityDescriptorTest {

    public static class TestClassA {

        @InjectLink
        protected String foo;

        @InjectLink
        private String bar;

        public String baz;
    }

    ResourceMappingContext mockRmc = new ResourceMappingContext() {

        @Override
        public ResourceMappingContext.Mapping getMapping(Class<?> resource) {
            return null;
        }
    };

    /**
     * Test for declared properties
     */
    @Test
    public void testDeclaredProperties() {
        System.out.println("Declared properties");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassA.class);
        assertEquals(2, instance.getLinkFields().size());
        assertEquals(1, instance.getNonLinkFields().size());
    }

    public static class TestClassB extends TestClassA {

        @InjectLink
        private String bar;
    }

    /**
     * Test for inherited properties
     */
    @Test
    public void testInheritedProperties() {
        System.out.println("Inherited properties");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassB.class);
        assertEquals(2, instance.getLinkFields().size());
        assertEquals(1, instance.getNonLinkFields().size());
    }

    private static final String TEMPLATE_A = "foo";

    @Path(TEMPLATE_A)
    public static class TestResourceA {
    }

    public static class TestClassC {

        @InjectLink(resource = TestResourceA.class, bindings = {@Binding(name = "bar", value = "baz")})
        String res;
    }

    @Test
    public void testResourceLink() {
        System.out.println("Resource class link");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassC.class);
        assertEquals(1, instance.getLinkFields().size());
        assertEquals(0, instance.getNonLinkFields().size());
        InjectLinkFieldDescriptor linkDesc = (InjectLinkFieldDescriptor) instance.getLinkFields().iterator().next();
        assertEquals(TEMPLATE_A, linkDesc.getLinkTemplate(mockRmc));
        assertEquals("baz", linkDesc.getBinding("bar"));
    }

    public static class TestClassD {

        @InjectLink(value = TEMPLATE_A, style = InjectLink.Style.RELATIVE_PATH)
        private String res1;

        @InjectLink(value = TEMPLATE_A, style = InjectLink.Style.RELATIVE_PATH)
        private URI res2;
    }

    @Test
    public void testStringLink() {
        System.out.println("String link");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassD.class);
        assertEquals(2, instance.getLinkFields().size());
        assertEquals(0, instance.getNonLinkFields().size());
        Iterator<FieldDescriptor> i = instance.getLinkFields().iterator();
        while (i.hasNext()) {
            InjectLinkFieldDescriptor linkDesc = (InjectLinkFieldDescriptor) i.next();
            assertEquals(TEMPLATE_A, linkDesc.getLinkTemplate(mockRmc));
        }
    }

    @Test
    public void testSetLink() {
        System.out.println("Set link");
        EntityDescriptor instance = EntityDescriptor.getInstance(TestClassD.class);
        Iterator<FieldDescriptor> i = instance.getLinkFields().iterator();
        TestClassD testClass = new TestClassD();
        while (i.hasNext()) {
            InjectLinkFieldDescriptor linkDesc = (InjectLinkFieldDescriptor) i.next();
            URI value = UriBuilder.fromPath(linkDesc.getLinkTemplate(mockRmc)).build();
            linkDesc.setPropertyValue(testClass, value);
        }
        assertEquals(TEMPLATE_A, testClass.res1);
        assertEquals(TEMPLATE_A, testClass.res2.toString());
    }

}
